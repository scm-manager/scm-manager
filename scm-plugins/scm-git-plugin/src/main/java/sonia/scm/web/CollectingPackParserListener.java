/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectIdSubclassMap;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.ReceivePack.PackParserListener;
import org.eclipse.jgit.transport.PackParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 * Implementation of {@link PackParserListener} to collect every object which is
 * pushed with the reveive pack. The listener is used to find out which object
 * is new and which was already pushed.
 *
 * @author Sebastian Sdorra
 */
public class CollectingPackParserListener implements PackParserListener
{

  /**
   * the logger for CollectingPackParserListener
   */
  private static final Logger logger =
    LoggerFactory.getLogger(CollectingPackParserListener.class);
  private final GitReceiveHook hook;

  public CollectingPackParserListener(GitReceiveHook hook) {
    this.hook = hook;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the listener from the receive pack.
   *
   *
   * @param pack receive pack
   *
   * @return listener
   */
  public static CollectingPackParserListener get(ReceivePack pack)
  {
    PackParserListener listener = pack.getPackParserListener();

    if (listener == null)
    {
      throw new IllegalArgumentException(
        "receive pack does not contain a listener");
    }

    Preconditions.checkArgument(
      listener instanceof CollectingPackParserListener,
      "listener is not a CollectingPackParserListener");

    return (CollectingPackParserListener) listener;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Applies the listener to the receive pack.
   *
   *
   * @param pack receive pack
   */
  public static void set(ReceivePack pack, GitReceiveHook hook)
  {
    logger.trace("apply collecting listener to receive pack");
    pack.setPackParserListener(new CollectingPackParserListener(hook));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Collects all new object ids.
   *
   *
   * @param parser pack parser
   */
  @Override
  public void after(PackParser parser)
  {
    logger.trace("retrieve new object ids from pack parser");

    ObjectIdSubclassMap<ObjectId> newObjectIdMap = parser.getNewObjectIds();

    if (newObjectIdMap != null)
    {
      newObjectIds = ImmutableSet.copyOf(newObjectIdMap);
      logger.debug("collected {} new object ids", newObjectIds.size());
    }
    else
    {
      logger.warn("pack parser returned no newObjectIds; no commit will be treated as a new one");
      newObjectIds = Set.of();
    }
  }

  /**
   * Prepares the pack parser to retrieve the new object ids.
   *
   *
   * @param parser pack parser
   */
  @Override
  public void before(PackParser parser)
  {
    logger.trace("prepare pack parser to collect new object ids");
    parser.setNeedNewObjectIds(true);
  }

  @Override
  public void release() {
    hook.afterReceive();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true} if the object is a new object.
   *
   * @param object rev object
   *
   * @return {@code true} if the object is new
   */
  public boolean isNew(RevObject object)
  {
    ensureAfterWasCalled();

    return newObjectIds.contains(object.getId());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Throws an {@link IllegalStateException} if the after method was not called.
   */
  private void ensureAfterWasCalled()
  {
    if (newObjectIds == null)
    {
      throw new IllegalStateException( "Pack parser seem not to be finished. "
        + "The receive pack has not called the after method of the listener.");
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** set of new object ids */
  private Set<ObjectId> newObjectIds;
}

/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectIdSubclassMap;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.transport.BaseReceivePack;
import org.eclipse.jgit.transport.BaseReceivePack.PackParserListener;
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the listener from the receive pack.
   *
   *
   * @param pack receive pack
   *
   * @return listener
   */
  public static CollectingPackParserListener get(BaseReceivePack pack)
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
  public static void set(BaseReceivePack pack)
  {
    logger.trace("apply collecting listener to receive pack");
    pack.setPackParserListener(new CollectingPackParserListener());
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
    }
    else
    {
      logger.warn("pack parser returned no newObjectIds");
      newObjectIds = ImmutableSet.of();
    }

    if (newObjectIds.isEmpty())
    {
      logger.debug("new object ids are empty, we treat every commit as new");
    }
    else
    {
      logger.debug("collected {} new object ids", newObjectIds.size());
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true} if the object is a new object. The method will also
   * return {@code true}, if the pack parser does not return a list with new
   * object ids.
   *
   *
   * @param object rev object
   *
   * @return {@code true} if the object is new
   */
  public boolean isNew(RevObject object)
  {
    ensureAfterWasCalled();

    return newObjectIds.isEmpty() || newObjectIds.contains(object.getId());
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

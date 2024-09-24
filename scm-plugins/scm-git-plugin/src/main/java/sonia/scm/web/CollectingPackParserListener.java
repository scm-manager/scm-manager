/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web;


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

import java.util.Set;

/**
 * Implementation of {@link PackParserListener} to collect every object which is
 * pushed with the reveive pack. The listener is used to find out which object
 * is new and which was already pushed.
 *
 */
public class CollectingPackParserListener implements PackParserListener
{

  private static final Logger logger =
    LoggerFactory.getLogger(CollectingPackParserListener.class);
  private final GitReceiveHook hook;

  private Set<ObjectId> newObjectIds;

  private CollectingPackParserListener(GitReceiveHook hook) {
    this.hook = hook;
  }


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


  /**
   * Returns {@code true} if the object is a new object.
   */
  public boolean isNew(RevObject object)
  {
    ensureAfterWasCalled();

    return newObjectIds.contains(object.getId());
  }


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

}

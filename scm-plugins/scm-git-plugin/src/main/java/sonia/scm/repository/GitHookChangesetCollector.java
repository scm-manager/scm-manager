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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.web.CollectingPackParserListener;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitHookChangesetCollector
{

  /**
   * the logger for GitHookChangesetCollector
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitHookChangesetCollector.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new instance
   *
   *
   * @param rpack
   * @param receiveCommands
   */
  public GitHookChangesetCollector(GitChangesetConverterFactory converterFactory, ReceivePack rpack,
    List<ReceiveCommand> receiveCommands)
  {
    this.converterFactory = converterFactory;
    this.rpack = rpack;
    this.receiveCommands = receiveCommands;
    this.listener = CollectingPackParserListener.get(rpack);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Collect all new changesets from the received hook.
   *
   * @return new changesets
   */
  public List<Changeset> collectChangesets()
  {
    Map<String, Changeset> changesets = Maps.newLinkedHashMap();

    try (
         org.eclipse.jgit.lib.Repository repository = rpack.getRepository();
         RevWalk walk = rpack.getRevWalk();
         GitChangesetConverter converter = converterFactory.create(repository, walk)
    ) {
      repository.incrementOpen();

      for (ReceiveCommand rc : receiveCommands)
      {
        String ref = rc.getRefName();

        logger.trace("handle receive command, type={}, ref={}, result={}", rc.getType(), ref, rc.getResult());

        if (rc.getType() == ReceiveCommand.Type.DELETE)
        {
          logger.debug("skip delete of ref {}", ref);
        }
        else if (! GitUtil.isBranch(ref))
        {
          logger.debug("skip ref {}, because it is not a branch", ref);
        }
        else
        {
          try
          {
            collectChangesets(changesets, converter, walk, rc);
          }
          catch (IOException ex)
          {
            StringBuilder builder = new StringBuilder();

            builder.append("could not handle receive command, type=");
            builder.append(rc.getType()).append(", ref=");
            builder.append(rc.getRefName()).append(", result=");
            builder.append(rc.getResult());

            logger.error(builder.toString(), ex);
          }
        }
      }

    }
    catch (Exception ex)
    {
      logger.error("could not collect changesets", ex);
    }

    return Lists.newArrayList(changesets.values());
  }

  private void collectChangesets(Map<String, Changeset> changesets,
    GitChangesetConverter converter, RevWalk walk, ReceiveCommand rc)
    throws IOException
  {
    ObjectId newId = rc.getNewId();

    String branch = GitUtil.getBranch(rc.getRefName());

    walk.reset();
    walk.sort(RevSort.TOPO);
    walk.sort(RevSort.REVERSE, true);

    logger.trace("mark {} as start for rev walk", newId.getName());

    walk.markStart(walk.parseCommit(newId));

    ObjectId oldId = rc.getOldId();

    if ((oldId != null) && !oldId.equals(ObjectId.zeroId()))
    {
      logger.trace("mark {} as uninteresting for rev walk", oldId.getName());

      walk.markUninteresting(walk.parseCommit(oldId));
    }

    RevCommit commit = walk.next();

    while (commit != null)
    {
      String id = commit.getId().name();
      Changeset changeset = changesets.get(id);

      if (changeset != null)
      {
        logger.trace(
          "commit {} already received durring this push, add branch {} to the commit",
          commit, branch);
        changeset.getBranches().add(branch);
      }
      else
      {

        // only append new commits
        if (listener.isNew(commit))
        {

          // parse commit body to avoid npe
          walk.parseBody(commit);

          changeset = converter.createChangeset(commit, branch);

          logger.trace("retrieve commit {} for hook", changeset.getId());

          changesets.put(id, changeset);
        }
        else
        {
          logger.trace("commit {} was already received", commit.getId());
        }
      }

      commit = walk.next();
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** listener to track new objects */
  private final CollectingPackParserListener listener;

  private final List<ReceiveCommand> receiveCommands;

  private final GitChangesetConverterFactory converterFactory;
  private final ReceivePack rpack;
}

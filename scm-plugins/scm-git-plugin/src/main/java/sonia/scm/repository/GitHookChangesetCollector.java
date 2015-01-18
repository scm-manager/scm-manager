/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;
import sonia.scm.web.CollectingPackParserListener;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;


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
   * Constructs ...
   *
   *
   * @param rpack
   * @param receiveCommands
   */
  public GitHookChangesetCollector(ReceivePack rpack,
    List<ReceiveCommand> receiveCommands)
  {
    this.rpack = rpack;
    this.receiveCommands = receiveCommands;
    this.listener = CollectingPackParserListener.get(rpack);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<Changeset> collectChangesets()
  {
    List<Changeset> changesets = Lists.newArrayList();

    org.eclipse.jgit.lib.Repository repository = rpack.getRepository();

    RevWalk walk = null;

    GitChangesetConverter converter = null;

    try
    {
      walk = rpack.getRevWalk();
      converter = new GitChangesetConverter(repository, walk);

      for (ReceiveCommand rc : receiveCommands)
      {
        //J-
        logger.trace("handle receive command, type={}, ref={}, result={}",
          new Object[] {
            rc.getType(),
            rc.getRefName(),
            rc.getResult()
          }
        );
        //J+

        if (rc.getType() != ReceiveCommand.Type.DELETE)
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
        else
        {
          logger.debug("skip delete of branch {}", rc.getRefName());
        }
      }

    }
    catch (Exception ex)
    {
      logger.error("could not collect changesets", ex);
    }
    finally
    {
      IOUtil.close(converter);
      GitUtil.release(walk);
    }

    return changesets;
  }

  /**
   * Method description
   *
   *
   * @param changesets
   * @param converter
   * @param walk
   * @param rc
   *
   * @throws IOException
   * @throws IncorrectObjectTypeException
   */
  private void collectChangesets(List<Changeset> changesets,
    GitChangesetConverter converter, RevWalk walk, ReceiveCommand rc)
    throws IncorrectObjectTypeException, IOException
  {
    ObjectId newId = rc.getNewId();

    String branch = GitUtil.getBranch(rc.getRefName());

    walk.reset();
    walk.sort(RevSort.TOPO);
    walk.sort(RevSort.REVERSE, true);

    logger.trace("mark {} as start for rev walk", newId.getName());

    walk.markStart(walk.parseCommit(newId));

    ObjectId oldId = rc.getOldId();

    if ((oldId != null) &&!oldId.equals(ObjectId.zeroId()))
    {
      logger.trace("mark {} as uninteresting for rev walk", oldId.getName());

      walk.markUninteresting(walk.parseCommit(oldId));
    }

    RevCommit commit = walk.next();

    List<String> branches = Lists.newArrayList(branch);

    while (commit != null)
    {

      // only append new commits
      if (listener.isNew(commit))
      {

        // parse commit body to avoid npe
        walk.parseBody(commit);

        Changeset changeset = converter.createChangeset(commit, branches);

        logger.trace("retrieve commit {} for hook", changeset.getId());

        changesets.add(changeset);
      }
      else
      {
        logger.trace("commit {} was already received", commit.getId());
      }

      commit = walk.next();
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** listener to track new objects */
  private final CollectingPackParserListener listener;

  /** Field description */
  private final List<ReceiveCommand> receiveCommands;

  /** Field description */
  private final ReceivePack rpack;
}

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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.web.CollectingPackParserListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Sebastian Sdorra
 */
class GitHookChangesetCollector {

  private static final Logger LOG = LoggerFactory.getLogger(GitHookChangesetCollector.class);

  /**
   * listener to track new objects
   */
  private final CollectingPackParserListener listener;

  private final List<ReceiveCommand> receiveCommands;

  private final GitChangesetConverterFactory converterFactory;
  private final ReceivePack rpack;

  private final Map<String, Changeset> addedChangesets = Maps.newLinkedHashMap();
  private final Map<String, Changeset> removedChangesets = Maps.newLinkedHashMap();

  GitHookChangesetCollector(GitChangesetConverterFactory converterFactory, ReceivePack rpack, List<ReceiveCommand> receiveCommands) {
    this.converterFactory = converterFactory;
    this.rpack = rpack;
    this.receiveCommands = receiveCommands;
    this.listener = CollectingPackParserListener.get(rpack);
  }

  /**
   * Collect all new changesets from the received hook. Afterwards, the results can be
   * retrieved with {@link #getAddedChangesets()} and {@link #getRemovedChangesets()}
   */
  void collectChangesets() {
    try (Repository repository = rpack.getRepository();
         RevWalk walk = rpack.getRevWalk();
         GitChangesetConverter converter = converterFactory.create(repository, walk)) {
      repository.incrementOpen();

      for (ReceiveCommand rc : receiveCommands) {
        String ref = rc.getRefName();

        LOG.trace("handle receive command, type={}, ref={}, result={}", rc.getType(), ref, rc.getResult());

        walk.reset();

        handle(repository, walk, converter, rc, ref);
      }
    } catch (Exception ex) {
      LOG.error("could not collect changesets", ex);
    }
  }

  List<Changeset> getAddedChangesets() {
    return Lists.newArrayList(addedChangesets.values());
  }

  List<Changeset> getRemovedChangesets() {
    return Lists.newArrayList(removedChangesets.values());
  }

  void handle(Repository repository, RevWalk walk, GitChangesetConverter converter, ReceiveCommand rc, String ref) {
    try {
      if (!(GitUtil.isBranch(ref) || GitUtil.isTag(ref))) {
        LOG.debug("skip ref {}, because it is neither branch nor tag", ref);
      } else if (rc.getType() == ReceiveCommand.Type.DELETE) {
        LOG.debug("handle deleted ref {}", ref);
        collectRemovedChangeset(repository, walk, converter, rc);
      } else {
        collectAddedChangesets(converter, walk, rc, ref);
      }
    } catch (IOException ex) {
      String message = "could not handle receive command, type=" +
        rc.getType() + ", ref=" +
        rc.getRefName() + ", result=" +
        rc.getResult();

      LOG.error(message, ex);
    }
  }

  private void collectAddedChangesets(GitChangesetConverter converter,
                                      RevWalk walk,
                                      ReceiveCommand rc,
                                      String ref)
    throws IOException {
    ObjectId newId = rc.getNewId();

    String branch = GitUtil.getBranch(rc.getRefName());

    walk.sort(RevSort.TOPO);
    walk.sort(RevSort.REVERSE, true);

    LOG.trace("mark {} as start for rev walk", newId.getName());

    walk.markStart(walk.parseCommit(newId));

    ObjectId oldId = rc.getOldId();

    if ((oldId != null) && !oldId.equals(ObjectId.zeroId())) {
      LOG.trace("mark {} as uninteresting for rev walk", oldId.getName());

      walk.markUninteresting(walk.parseCommit(oldId));
    }

    RevCommit commit = walk.next();

    while (commit != null) {
      String id = commit.getId().name();
      Changeset changeset = addedChangesets.get(id);

      if (changeset != null) {
        if (GitUtil.isBranch(ref)) {
          LOG.trace(
            "commit {} already received during this push, add branch {} to the commit",
            commit, branch);
          changeset.getBranches().add(branch);
        }
      } else if (listener.isNew(commit)) {
        // only append new commits
        addToCollection(addedChangesets, converter, walk, commit, id, branch);
      } else {
        LOG.trace("commit {} was already received", commit.getId());
      }

      commit = walk.next();
    }
  }

  private void collectRemovedChangeset(Repository repository, RevWalk walk, GitChangesetConverter converter, ReceiveCommand rc) throws IOException {
    ObjectId oldId = rc.getOldId();

    walk.markStart(walk.parseCommit(oldId));
    GitUtil.getAllCommits(repository, walk).forEach(c -> {
      try {
        walk.markUninteresting(c);
      } catch (IOException e) {
        throw new IllegalStateException("failed to mark commit as to be ignored", e);
      }
    });

    RevCommit commit = walk.next();

    while (commit != null) {
      String id = commit.getId().name();
      Changeset changeset = removedChangesets.get(id);

      if (changeset == null) {
        addToCollection(removedChangesets, converter, walk, commit, id);
      }

      commit = walk.next();
    }
  }

  private void addToCollection(Map<String, Changeset> changesets, GitChangesetConverter converter, RevWalk walk, RevCommit commit, String id, String... branches) throws IOException {
    // parse commit body to avoid npe
    walk.parseBody(commit);

    Changeset newChangeset = converter.createChangeset(commit, branches);

    LOG.trace("retrieve commit {} for hook", newChangeset.getId());

    changesets.put(id, newChangeset);
  }
}

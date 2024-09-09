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

package sonia.scm.repository.client.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.client.api.RepositoryClientException;

import java.io.IOException;

public class GitMergeCommand implements MergeCommand {

  private final Git git;

  GitMergeCommand(Git git) {
    this.git = git;
  }

  @Override
  public Changeset merge(MergeRequest request) throws IOException {
    try (GitChangesetConverter converter = GitTestHelper.createConverterFactory().create(git.getRepository())) {
      ObjectId resolved = git.getRepository().resolve(request.getBranch());
      org.eclipse.jgit.api.MergeCommand mergeCommand = git.merge()
        .include(request.getBranch(), resolved)
        .setMessage(request.getMessage());

      switch (request.getFfMode()) {
        case FF:
          mergeCommand.setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.FF);
          break;
        case NO_FF:
          mergeCommand.setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.NO_FF);
          break;
        case FF_ONLY:
          mergeCommand.setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.FF_ONLY);
          break;
        default:
          throw new IllegalStateException("Unknown FF mode: " + request.getFfMode());
      }

      MergeResult mergeResult = mergeCommand
        .call();

      try (RevWalk revWalk = new RevWalk(git.getRepository())) {
        RevCommit commit = revWalk.parseCommit(mergeResult.getNewHead());
        return converter.createChangeset(commit);
      }
    } catch (GitAPIException ex) {
      throw new RepositoryClientException("could not commit changes to repository", ex);
    }
  }
}

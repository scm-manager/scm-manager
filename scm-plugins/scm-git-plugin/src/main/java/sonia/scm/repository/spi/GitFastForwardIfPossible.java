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

package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;
import java.util.Collections;

class GitFastForwardIfPossible extends GitMergeStrategy {

  private GitMergeStrategy fallbackMerge;

  GitFastForwardIfPossible(Git clone, MergeCommandRequest request, GitContext context, Repository repository) {
    super(clone, request, context, repository);
    fallbackMerge = new GitMergeCommit(clone, request, context, repository);
  }

  @Override
  MergeCommandResult run() throws IOException {
    MergeResult fastForwardResult = mergeWithFastForwardOnlyMode();
    if (fastForwardResult.getMergeStatus().isSuccessful()) {
      push();
      return createSuccessResult(fastForwardResult.getNewHead().name());
    } else {
      return fallbackMerge.run();
    }
  }

  private MergeResult mergeWithFastForwardOnlyMode() throws IOException {
    MergeCommand mergeCommand = getClone().merge();
    mergeCommand.setFastForward(MergeCommand.FastForwardMode.FF_ONLY);
    return doMergeInClone(mergeCommand);
  }
}

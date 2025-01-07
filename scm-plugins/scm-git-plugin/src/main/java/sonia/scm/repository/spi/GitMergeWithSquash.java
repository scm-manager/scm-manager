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

import org.eclipse.jgit.lib.ObjectId;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.MergeCommandResult;

class GitMergeWithSquash {

  private final MergeCommandRequest request;
  private final MergeHelper mergeHelper;

  GitMergeWithSquash(MergeCommandRequest request, GitContext context, RepositoryManager repositoryManager, GitRepositoryHookEventFactory eventFactory) {
    this.request = request;
    this.mergeHelper = new MergeHelper(context, request, repositoryManager, eventFactory);
  }

  MergeCommandResult run() {
    return mergeHelper.doRecursiveMerge(request, (sourceRevision, targetRevision) -> new ObjectId[]{targetRevision});
  }
}

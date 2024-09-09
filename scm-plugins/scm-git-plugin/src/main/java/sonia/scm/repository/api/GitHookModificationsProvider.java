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

package sonia.scm.repository.api;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Added;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Removed;
import sonia.scm.repository.spi.BranchBasedModificationsComputer;
import sonia.scm.repository.spi.ModificationsComputer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Computes modifications for created, modified and deleted git branches during a hook.
 */
public class GitHookModificationsProvider implements HookModificationsProvider {

  private static final Logger logger = LoggerFactory.getLogger(GitHookModificationsProvider.class);
  private final org.eclipse.jgit.lib.Repository repository;
  private final Map<String, BranchEntry> modificationsCommandRequests;

  public GitHookModificationsProvider(List<ReceiveCommand> commands, org.eclipse.jgit.lib.Repository repository) {
    this.repository = repository;
    ImmutableMap.Builder<String, BranchEntry> modificationsCommandRequestBuilder = ImmutableMap.builder();

    for (ReceiveCommand command : commands) {
      String ref = command.getRefName();
      String branch = GitUtil.getBranch(ref);
      if (Strings.isNullOrEmpty(branch)) {
        logger.debug("ref {} is not a branch", ref);
      } else if (command.getType() == ReceiveCommand.Type.UPDATE || command.getType() == ReceiveCommand.Type.UPDATE_NONFASTFORWARD) {
        modificationsCommandRequestBuilder.put(branch, new UpdateBranchEntry(command.getNewId().name(), command.getOldId().name()));
      } else if (command.getType() == ReceiveCommand.Type.CREATE) {
        modificationsCommandRequestBuilder.put(branch, new CreateBranchEntry(command.getNewId()));
      } else if (command.getType() == ReceiveCommand.Type.DELETE) {
        modificationsCommandRequestBuilder.put(branch, new DeleteBranchEntry(command.getOldId()));
      }
    }

    modificationsCommandRequests = modificationsCommandRequestBuilder.build();
  }

  @Override
  public Modifications getModifications(String branchName) {
    BranchEntry branchEntry = modificationsCommandRequests.get(branchName);
    try {
      return branchEntry.getModifications();
    } catch (IOException ex) {
      throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity("Git Repository", repository.toString()), "could not compute diff for branch " + branchName, ex);
    }
  }

  private interface BranchEntry {
    Modifications getModifications() throws IOException;
  }

  @AllArgsConstructor
  private class UpdateBranchEntry implements BranchEntry {
    private final String newRevision;
    private final String oldRevision;

    @Override
    public Modifications getModifications() throws IOException {
      return new Modifications(
        oldRevision,
        newRevision,
        new ModificationsComputer(repository).compute(oldRevision, newRevision).getModifications()
      );
    }
  }

  @AllArgsConstructor
  private class DeleteBranchEntry implements BranchEntry {
    private final ObjectId oldRevision;

    @Override
    public Modifications getModifications() throws IOException {
      return createModifications(oldRevision, Removed::new);
    }
  }

  @AllArgsConstructor
  private class CreateBranchEntry implements BranchEntry {
    private final ObjectId newRevision;

    @Override
    public Modifications getModifications() throws IOException {
      return createModifications(newRevision, Added::new);
    }
  }

  private Modifications createModifications(ObjectId revision, Function<String, Modification> modificationFactory) throws IOException {
    return new BranchBasedModificationsComputer(repository).createModifications(revision, modificationFactory);
  }
}

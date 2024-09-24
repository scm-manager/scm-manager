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

package sonia.scm.repository;

import com.github.legman.Subscribe;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.CannotDeleteDefaultBranchException;

import java.io.IOException;
import java.util.List;

@Extension
@EagerSingleton
public class DefaultBranchDeleteProtection {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultBranchDeleteProtection.class);

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public DefaultBranchDeleteProtection(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  @Subscribe(async = false)
  public void protectDefaultBranch(PreReceiveRepositoryHookEvent event) {
    if (event.getContext().isFeatureSupported(HookFeature.BRANCH_PROVIDER)) {
      List<String> deletedOrClosed = event.getContext().getBranchProvider().getDeletedOrClosed();
      if (!deletedOrClosed.isEmpty()) {
        checkDeletedBranches(event, deletedOrClosed);
      }
    }
  }

  private void checkDeletedBranches(PreReceiveRepositoryHookEvent event, List<String> deletedOrClosed) {
    try (RepositoryService service = serviceFactory.create(event.getRepository())) {
      getBranches(service)
        .getBranches()
        .stream()
        .filter(Branch::isDefaultBranch)
        .findFirst()
        .ifPresent(
          defaultBranch -> assertBranchNotDeleted(event, deletedOrClosed, defaultBranch)
        );
    }
  }

  private Branches getBranches(RepositoryService service) {
    try {
      return service.getBranchesCommand().setDisableCache(true).getBranches();
    } catch (IOException e) {
      LOG.warn("Could not read branches in repository {} to check for default branch", service.getRepository());
      return new Branches();
    }
  }

  private void assertBranchNotDeleted(PreReceiveRepositoryHookEvent event, List<String> deletedOrClosed, Branch defaultBranch) {
    String defaultBranchName = defaultBranch.getName();
    if (deletedOrClosed.stream().anyMatch(branch -> branch.equals(defaultBranchName))) {
      throw new CannotDeleteDefaultBranchException(event.getRepository(), defaultBranchName);
    }
  }
}

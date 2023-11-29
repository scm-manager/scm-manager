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

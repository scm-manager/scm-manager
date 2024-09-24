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
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.store.ConfigurationStore;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Extension
@EagerSingleton
public class GitRepositoryConfigInitializer {

  private final GitRepositoryHandler repoHandler;
  private final GitRepositoryConfigStoreProvider storeProvider;
  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public GitRepositoryConfigInitializer(GitRepositoryHandler repoHandler, GitRepositoryConfigStoreProvider storeProvider, RepositoryServiceFactory serviceFactory) {
    this.repoHandler = repoHandler;
    this.storeProvider = storeProvider;
    this.serviceFactory = serviceFactory;
  }

  @Subscribe
  public void initConfig(PostReceiveRepositoryHookEvent event) throws IOException {
    if (GitRepositoryHandler.TYPE_NAME.equals(event.getRepository().getType())) {
      ConfigurationStore<GitRepositoryConfig> store = storeProvider.get(event.getRepository());
      GitRepositoryConfig repositoryConfig = store.get();
      if (repositoryConfig == null || Strings.isNullOrEmpty(repositoryConfig.getDefaultBranch())) {
        String defaultBranch;
        try (RepositoryService service = serviceFactory.create(event.getRepository())) {
          List<Branch> branches = service.getBranchesCommand().getBranches().getBranches();
          Optional<Branch> repoDefaultBranch = branches.stream().filter(Branch::isDefaultBranch).findFirst();
          if (repoDefaultBranch.isPresent()) {
            defaultBranch = repoDefaultBranch.get().getName();
          } else {
            defaultBranch = determineDefaultBranchFromPush(event);
          }
        }

        GitRepositoryConfig gitRepositoryConfig = new GitRepositoryConfig(defaultBranch);
        store.set(gitRepositoryConfig);
      }
    }
  }

  private String determineDefaultBranchFromPush(PostReceiveRepositoryHookEvent event) {
    List<String> defaultBranchCandidates = event.getContext().getBranchProvider().getCreatedOrModified();
    String globalConfigDefaultBranch = repoHandler.getConfig().getDefaultBranch();
    if (defaultBranchCandidates.contains(globalConfigDefaultBranch)) {
      return globalConfigDefaultBranch;
    }

    if (defaultBranchCandidates.contains("main")) {
      return "main";
    }

    if (defaultBranchCandidates.contains("master")) {
      return "master";
    }

    return defaultBranchCandidates.stream()
      .filter(b -> !b.contains("/"))
      .sorted(Comparator.comparing(String::length))
      .findAny().orElse(defaultBranchCandidates.get(0));
  }
}

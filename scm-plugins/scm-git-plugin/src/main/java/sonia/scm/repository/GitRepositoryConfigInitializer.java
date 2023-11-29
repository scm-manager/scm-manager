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
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStore;

import java.util.Comparator;
import java.util.List;

@Extension
@EagerSingleton
public class GitRepositoryConfigInitializer {

  private final GitRepositoryHandler repoHandler;
  private final GitRepositoryConfigStoreProvider storeProvider;

  @Inject
  public GitRepositoryConfigInitializer(GitRepositoryHandler repoHandler, GitRepositoryConfigStoreProvider storeProvider) {
    this.repoHandler = repoHandler;
    this.storeProvider = storeProvider;
  }

  @Subscribe
  public void initConfig(PostReceiveRepositoryHookEvent event) {
    if (GitRepositoryHandler.TYPE_NAME.equals(event.getRepository().getType())) {
      ConfigurationStore<GitRepositoryConfig> store = storeProvider.get(event.getRepository());
      GitRepositoryConfig repositoryConfig = store.get();
      if (repositoryConfig == null || Strings.isNullOrEmpty(repositoryConfig.getDefaultBranch())) {
        List<String> defaultBranchCandidates = event.getContext().getBranchProvider().getCreatedOrModified();

        String defaultBranch = determineDefaultBranch(defaultBranchCandidates);

        GitRepositoryConfig gitRepositoryConfig = new GitRepositoryConfig(defaultBranch);
        store.set(gitRepositoryConfig);
      }
    }
  }

  private String determineDefaultBranch(List<String> defaultBranchCandidates) {
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

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
import sonia.scm.EagerSingleton;
import sonia.scm.api.v2.resources.GitRepositoryConfigChangedEvent;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.plugin.Extension;

/**
 * Repository listener which handles git related repository events.
 *
 * @since 1.50
 */
@Extension
@EagerSingleton
public class GitRepositoryModifyListener {

  private final GitHeadModifier headModifier;
  private final GitRepositoryConfigStoreProvider storeProvider;

  @Inject
  public GitRepositoryModifyListener(GitHeadModifier headModifier, GitRepositoryConfigStoreProvider storeProvider) {
    this.headModifier = headModifier;
    this.storeProvider = storeProvider;
  }

  @Subscribe
  public void handleEvent(GitRepositoryConfigChangedEvent event){
    Repository repository = event.getRepository();

    String defaultBranch = storeProvider.get(repository).get().getDefaultBranch();
    if (defaultBranch != null) {
      headModifier.ensure(repository, defaultBranch);
    }
  }
}

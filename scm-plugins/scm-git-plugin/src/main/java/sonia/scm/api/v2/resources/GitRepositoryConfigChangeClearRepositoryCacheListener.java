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

package sonia.scm.api.v2.resources;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.ClearRepositoryCacheEvent;
import sonia.scm.repository.RepositoryModificationEvent;

import java.util.Objects;

@Extension
@EagerSingleton
public class GitRepositoryConfigChangeClearRepositoryCacheListener {

  /**
   * Receives {@link RepositoryModificationEvent} and fires a {@link ClearRepositoryCacheEvent} if
   * the default branch of a git repository was modified.
   *
   * @param event repository modification event
   */
  @Subscribe
  public void sendClearRepositoryCacheEvent(GitRepositoryConfigChangedEvent event) {
    if (!Objects.equals(event.getOldConfig().getDefaultBranch(), event.getNewConfig().getDefaultBranch())) {
      ScmEventBus.getInstance().post(new ClearRepositoryCacheEvent(event.getRepository()));
    }
  }
}

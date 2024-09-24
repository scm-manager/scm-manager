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
import sonia.scm.api.v2.resources.GitRepositoryConfigChangedEvent;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;

@Extension
@EagerSingleton
public class DefaultBranchChangedDispatcher {

  private final ScmEventBus eventBus;

  @Inject
  public DefaultBranchChangedDispatcher(ScmEventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Subscribe
  public void handleConfigUpdate(GitRepositoryConfigChangedEvent event) {
    String oldDefaultBranch = event.getOldConfig().getDefaultBranch();
    String newDefaultBranch = event.getNewConfig().getDefaultBranch();
    if (hasChanged(oldDefaultBranch, newDefaultBranch)) {
      eventBus.post(new DefaultBranchChangedEvent(event.getRepository(), oldDefaultBranch, newDefaultBranch));
    }
  }

  private boolean hasChanged(String oldDefaultBranch, String newDefaultBranch) {
    return !Strings.nullToEmpty(oldDefaultBranch).equals(Strings.nullToEmpty(newDefaultBranch));
  }
}

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
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.api.HookFeature;

import java.util.Set;

@Slf4j
@Extension
@EagerSingleton
public class TagProtectionPreReceiveRepositoryHook {

  private final Set<TagGuard> tagGuards;

  @Inject
  public TagProtectionPreReceiveRepositoryHook(Set<TagGuard> tagGuards) {
    this.tagGuards = tagGuards;
  }

  @Subscribe(async = false)
  public void onEvent(PreReceiveRepositoryHookEvent event) {
    if (tagGuards.isEmpty()) {
      log.trace("no tag guards available, skipping tag protection");
      return;
    }
    Repository repository = event.getRepository();
    if (repository == null) {
      log.trace("received hook without repository, skipping tag protection");
      return;
    }
    if (!event.getContext().isFeatureSupported(HookFeature.TAG_PROVIDER)) {
      log.trace("repository {} does not support tag provider, skipping tag protection", repository);
      return;
    }

    event
      .getContext()
      .getTagProvider()
      .getDeletedTags()
      .forEach(tag -> {
        boolean tagMustBeProtected = tagGuards.stream().anyMatch(guard -> !guard.canDelete(new TagGuardDeletionRequest(repository, tag)));
        if (tagMustBeProtected) {
          String message = String.format("Deleting tag '%s' not allowed in repository %s", tag.getName(), repository);
          log.info(message);

          if (event.getContext().isFeatureSupported(HookFeature.MESSAGE_PROVIDER)) {
            event.getContext().getMessageProvider().sendMessage(message);
          }
          throw new TagProtectionException(repository, tag, message);
        } else {
          log.trace("tag {} does not have to be protected", tag);
        }
      });
  }
}

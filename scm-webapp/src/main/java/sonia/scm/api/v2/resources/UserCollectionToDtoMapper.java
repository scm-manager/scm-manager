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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import sonia.scm.PageResult;
import sonia.scm.user.ExternalAuthenticationAvailableNotifier;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class UserCollectionToDtoMapper extends BasicCollectionToDtoMapper<User, UserDto, UserToUserDtoMapper> {

  private final ResourceLinks resourceLinks;

  private final Set<ExternalAuthenticationAvailableNotifier> externalAuthenticationAvailableNotifier;

  @Inject
  public UserCollectionToDtoMapper(UserToUserDtoMapper userToDtoMapper, ResourceLinks resourceLinks, Set<ExternalAuthenticationAvailableNotifier> externalAuthenticationAvailableNotifier) {
    super("users", userToDtoMapper);
    this.resourceLinks = resourceLinks;
    this.externalAuthenticationAvailableNotifier = externalAuthenticationAvailableNotifier;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<User> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(), this.createCreateLink());
  }

  Optional<String> createCreateLink() {
    return UserPermissions.create().isPermitted() ? of(resourceLinks.userCollection().create()) : empty();
  }

  String createSelfLink() {
    return resourceLinks.userCollection().self();
  }

  @Override
  CollectionDto createCollectionDto(Links links, Embedded embedded) {
    return new UserCollectionDto(links, embedded, isExternalAuthenticationAvailable());
  }


  boolean isExternalAuthenticationAvailable() {
    for (ExternalAuthenticationAvailableNotifier externalAuthenticationAvailable : externalAuthenticationAvailableNotifier) {
      if (externalAuthenticationAvailable.isExternalAuthenticationAvailable()) {
        return true;
      }
    }
    return false;
  }
}



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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.group.GroupPermissions;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserPermissions;
import sonia.scm.web.EdisonHalAppender;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class UserToUserDtoMapper extends BaseMapper<User, UserDto> {

  @Inject
  private UserManager userManager;
  @Inject
  private ScmConfigurationStore scmConfigurationStore;

  @Override
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "password", ignore = true)
  public abstract UserDto map(User modelObject);

  @Inject
  private ResourceLinks resourceLinks;

  @ObjectFactory
  UserDto createDto(User user) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.user().self(user.getName()));
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.user().delete(user.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.user().update(user.getName())));
      linksBuilder.single(link("publicKeys", resourceLinks.user().publicKeys(user.getName())));
      if (scmConfigurationStore.get().isEnabledApiKeys()) {
        linksBuilder.single(link("apiKeys", resourceLinks.user().apiKeys(user.getName())));
      }
      if (user.isExternal()) {
        linksBuilder.single(link("convertToInternal", resourceLinks.user().toInternal(user.getName())));
      } else {
        linksBuilder.single(link("password", resourceLinks.user().passwordChange(user.getName())));
        linksBuilder.single(link("convertToExternal", resourceLinks.user().toExternal(user.getName())));
      }
    }
    if (PermissionPermissions.read().isPermitted()) {
      linksBuilder.single(link("permissions", resourceLinks.userPermissions().permissions(user.getName())));
      if (GroupPermissions.list().isPermitted()) {
        linksBuilder.single(link("permissionOverview", resourceLinks.user().permissionOverview(user.getName())));
      }
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), user);

    return new UserDto(linksBuilder.build(), embeddedBuilder.build());
  }

}

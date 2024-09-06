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

import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.PermissionPermissions;

import java.util.Collection;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

public class PermissionCollectionToDtoMapper {

  private final ResourceLinks resourceLinks;

  @Inject
  public PermissionCollectionToDtoMapper(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public PermissionListDto mapForUser(Collection<PermissionDescriptor> permissions, String userId) {
    return map(permissions, userId, resourceLinks.userPermissions());
  }

  public PermissionListDto mapForGroup(Collection<PermissionDescriptor> permissions, String groupId) {
    return map(permissions, groupId, resourceLinks.groupPermissions());
  }

  private PermissionListDto map(Collection<PermissionDescriptor> permissions, String id, ResourceLinks.WithPermissionLinks links) {
    String[] permissionStrings = permissions
      .stream()
      .map(PermissionDescriptor::getValue)
      .toArray(String[]::new);
    PermissionListDto target = new PermissionListDto(permissionStrings);

    Links.Builder linksBuilder = linkingTo().self(links.permissions(id));

    if (PermissionPermissions.assign().isPermitted()) {
      linksBuilder.single(link("overwrite", links.overwritePermissions(id)));
    }

    target.add(linksBuilder.build());

    return target;
  }
}

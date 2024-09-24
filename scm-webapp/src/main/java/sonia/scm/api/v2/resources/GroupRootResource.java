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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Path;

/**
 * RESTful Web Service Resource to manage groups and their members.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "Group", description = "Group related endpoints")
})
@Path(GroupRootResource.GROUPS_PATH_V2)
public class GroupRootResource {

  static final String GROUPS_PATH_V2 = "v2/groups/";

  private final Provider<GroupCollectionResource> groupCollectionResource;
  private final Provider<GroupResource> groupResource;

  @Inject
  public GroupRootResource(Provider<GroupCollectionResource> groupCollectionResource,
                           Provider<GroupResource> groupResource) {
    this.groupCollectionResource = groupCollectionResource;
    this.groupResource = groupResource;
  }

  @Path("")
  public GroupCollectionResource getGroupCollectionResource() {
    return groupCollectionResource.get();
  }

  @Path("{id}")
  public GroupResource getGroupResource() {
    return groupResource.get();
  }
}

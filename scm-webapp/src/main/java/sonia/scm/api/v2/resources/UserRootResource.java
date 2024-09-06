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
 * RESTful Web Service Resource to manage users.
 */
@OpenAPIDefinition(tags = {
  @Tag(name = "User", description = "User related endpoints")
})
@Path(UserRootResource.USERS_PATH_V2)
public class UserRootResource {

  static final String USERS_PATH_V2 = "v2/users/";

  private final Provider<UserCollectionResource> userCollectionResource;
  private final Provider<UserResource> userResource;
  private final Provider<UserApiKeyResource> userApiKeyResource;

  @Inject
  public UserRootResource(Provider<UserCollectionResource> userCollectionResource,
                          Provider<UserResource> userResource, Provider<UserApiKeyResource> userApiKeyResource) {
    this.userCollectionResource = userCollectionResource;
    this.userResource = userResource;
    this.userApiKeyResource = userApiKeyResource;
  }

  @Path("")
  public UserCollectionResource getUserCollectionResource() {
    return userCollectionResource.get();
  }

  @Path("{id}")
  public UserResource getUserResource() {
    return userResource.get();
  }

  @Path("{id}/api_keys")
  public UserApiKeyResource apiKeys() {
    return userApiKeyResource.get();
  }
}

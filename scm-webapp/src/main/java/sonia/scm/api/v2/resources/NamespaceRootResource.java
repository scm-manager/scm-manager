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
 * RESTful Web Service Resource to manage namespaces.
 */
@OpenAPIDefinition(
  tags = {
    @Tag(name = "Namespace", description = "Namespace related endpoints")
  }
)
@Path(NamespaceRootResource.NAMESPACE_PATH_V2)
public class NamespaceRootResource {
  static final String NAMESPACE_PATH_V2 = "v2/namespaces/";

  private final Provider<NamespaceCollectionResource> namespaceCollectionResource;
  private final Provider<NamespaceResource> namespaceResource;

  @Inject
  public NamespaceRootResource(Provider<NamespaceCollectionResource> namespaceCollectionResource, Provider<NamespaceResource> namespaceResource) {
    this.namespaceCollectionResource = namespaceCollectionResource;
    this.namespaceResource = namespaceResource;
  }

  @Path("{namespace}")
  public NamespaceResource getNamespaceResource() {
    return namespaceResource.get();
  }

  @Path("")
  public NamespaceCollectionResource getNamespaceCollectionResource() {
    return namespaceCollectionResource.get();
  }
}

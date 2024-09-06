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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class NamespaceResource {

  private final NamespaceManager manager;
  private final NamespaceToNamespaceDtoMapper namespaceMapper;
  private final Provider<NamespacePermissionResource> namespacePermissionResource;

  @Inject
  public NamespaceResource(NamespaceManager manager, NamespaceToNamespaceDtoMapper namespaceMapper, Provider<NamespacePermissionResource> namespacePermissionResource) {
    this.manager = manager;
    this.namespaceMapper = namespaceMapper;
    this.namespacePermissionResource = namespacePermissionResource;
  }

  /**
   * Returns a namespace.
   *
   * @param namespace the requested namespace
   */
  @GET
  @Path("")
  @Produces(VndMediaType.NAMESPACE)
  @Operation(summary = "Get single namespace", description = "Returns the namespace for the given name.", tags = "Namespace")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.NAMESPACE,
      schema = @Schema(implementation = NamespaceDto.class)
    )
  )
  @ApiResponse(
    responseCode = "401",
    description = "not authenticated / invalid credentials"
  )
  @ApiResponse(
    responseCode = "404",
    description = "not found, no namespace with the specified name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public NamespaceDto get(@PathParam("namespace") String namespace) {
    return manager.get(namespace)
      .map(namespaceMapper::map)
      .orElseThrow(() -> notFound(entity("Namespace", namespace)));
  }

  @Path("permissions")
  public NamespacePermissionResource permissions() {
    return namespacePermissionResource.get();
  }
}

/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

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

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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import sonia.scm.repository.HgRepositoryConfigStore;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.HgVndMediaType;
import sonia.scm.web.VndMediaType;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class HgRepositoryConfigResource {

  private final RepositoryManager repositoryManager;
  private final HgRepositoryConfigStore store;
  private final HgRepositoryConfigMapper mapper;

  @Inject
  public HgRepositoryConfigResource(RepositoryManager repositoryManager, HgRepositoryConfigStore store, HgRepositoryConfigMapper mapper) {
    this.repositoryManager = repositoryManager;
    this.store = store;
    this.mapper = mapper;
  }

  @GET
  @Path("")
  @Produces(HgVndMediaType.REPO_CONFIG)
  @Operation(
    summary = "Hg configuration",
    description = "Returns the global mercurial configuration.",
    tags = "Mercurial",
    operationId = "hg_get_repo_config"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = HgVndMediaType.REPO_CONFIG,
      schema = @Schema(implementation = HgGlobalGlobalConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository:read:{repositoryId}\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public HgRepositoryConfigDto getHgRepositoryConfig(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = getRepository(namespace, name);
    return mapper.map(repository, store.of(repository));
  }

  @PUT
  @Path("")
  @Consumes(HgVndMediaType.REPO_CONFIG)
  @Operation(
    summary = "Modify hg configuration",
    description = "Modifies the repository specific mercurial configuration.",
    tags = "Mercurial",
    operationId = "hg_put_repo_config",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = HgVndMediaType.CONFIG,
        schema = @Schema(implementation = UpdateHgGlobalConfigDto.class),
        examples = @ExampleObject(
          name = "Overwrites current configuration with this one.",
          value = "{\n  \"encoding\":\"UTF-8\" \n}",
          summary = "Simple update configuration"
        )
      )
    )
  )
  @ApiResponse(
    responseCode = "204",
    description = "update success"
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository:hg:{repositoryId}\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response updateHgRepositoryConfig(
    @PathParam("namespace") String namespace,
    @PathParam("name") String name,
    @Valid HgRepositoryConfigDto dto
  ) {
    Repository repository = getRepository(namespace, name);
    store.store(repository, mapper.map(dto));
    return Response.noContent().build();
  }

  private Repository getRepository(String namespace, String name) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      throw notFound(entity(namespaceAndName));
    }
    return repository;
  }

}

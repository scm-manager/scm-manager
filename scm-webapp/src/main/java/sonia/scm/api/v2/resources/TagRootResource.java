/**
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
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.Tag;
import sonia.scm.repository.Tags;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class TagRootResource {

  private final RepositoryServiceFactory serviceFactory;
  private final TagCollectionToDtoMapper tagCollectionToDtoMapper;
  private final TagToTagDtoMapper tagToTagDtoMapper;

  @Inject
  public TagRootResource(RepositoryServiceFactory serviceFactory,
                         TagCollectionToDtoMapper tagCollectionToDtoMapper,
                         TagToTagDtoMapper tagToTagDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.tagCollectionToDtoMapper = tagCollectionToDtoMapper;
    this.tagToTagDtoMapper = tagToTagDtoMapper;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.TAG_COLLECTION)
  @Operation(summary = "List of tags", description = "Returns the tags for the given namespace and name.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.TAG_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the tags")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Tags tags = getTags(repositoryService);
      if (tags != null && tags.getTags() != null) {
        return Response.ok(tagCollectionToDtoMapper.map(namespace, name, tags.getTags())).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Error on getting tag from repository.")
          .build();
      }
    }
  }


  @GET
  @Path("{tagName}")
  @Produces(VndMediaType.TAG)
  @Operation(summary = "Get tag", description = "Returns the tag for the given name.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.TAG,
      schema = @Schema(implementation = TagDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user has no privileges to read the tags")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no tag with the specified name available in the repository",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("tagName") String tagName) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      Tags tags = getTags(repositoryService);
      if (tags != null && tags.getTags() != null) {
        Tag tag = tags.getTags().stream()
          .filter(t -> tagName.equals(t.getName()))
          .findFirst()
          .orElseThrow(() -> createNotFoundException(namespace, name, tagName));
        return Response.ok(tagToTagDtoMapper.map(tag, namespaceAndName)).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Error on getting tag from repository.")
          .build();
      }
    }
  }

  private NotFoundException createNotFoundException(String namespace, String name, String tagName) {
    return notFound(entity("Tag", tagName).in("Repository", namespace + "/" + name));
  }

  private Tags getTags(RepositoryService repositoryService) throws IOException {
    Repository repository = repositoryService.getRepository();
    RepositoryPermissions.read(repository).check();
    return repositoryService.getTagsCommand().getTags();
  }


}

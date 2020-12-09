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
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.Tag;
import sonia.scm.repository.Tags;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.TagCommandBuilder;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class TagRootResource {

  private final RepositoryServiceFactory serviceFactory;
  private final TagCollectionToDtoMapper tagCollectionToDtoMapper;
  private final TagToTagDtoMapper tagToTagDtoMapper;
  private final ResourceLinks resourceLinks;

  @Inject
  public TagRootResource(RepositoryServiceFactory serviceFactory,
                         TagCollectionToDtoMapper tagCollectionToDtoMapper,
                         TagToTagDtoMapper tagToTagDtoMapper,
                         ResourceLinks resourceLinks) {
    this.serviceFactory = serviceFactory;
    this.tagCollectionToDtoMapper = tagCollectionToDtoMapper;
    this.tagToTagDtoMapper = tagToTagDtoMapper;
    this.resourceLinks = resourceLinks;
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
        return Response.ok(tagCollectionToDtoMapper.map(tags.getTags(), repositoryService.getRepository())).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Error on getting tag from repository.")
          .build();
      }
    }
  }

  @POST
  @Path("")
  @Produces(VndMediaType.TAG_REQUEST)
  @Operation(summary = "Create tag",
    description = "Creates a new tag.",
    tags = "Repository",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.TAG_REQUEST,
        schema = @Schema(implementation = TagRequestDto.class),
        examples = @ExampleObject(
          name = "Create a new tag for a revision",
          value = "{\n  \"revision\":\"734713bc047d87bf7eac9674765ae793478c50d3\",\n  \"name\":\"v1.1.0\"\n}",
          summary = "Create a tag"
        )
      )
    ))
  @ApiResponse(
    responseCode = "201",
    description = "create success",
    headers = @Header(
      name = "Location",
      description = "uri to the created tag",
      schema = @Schema(type = "string")
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
  public Response create(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid TagRequestDto tagRequest) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    String revision = tagRequest.getRevision();
    String tagName = tagRequest.getName();
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      if (tagExists(tagName, repositoryService)) {
        throw alreadyExists(entity(Tag.class, tagName).in(repositoryService.getRepository()));
      }
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.push(repository).check();
      TagCommandBuilder tagCommandBuilder = repositoryService.getTagCommand();
      final Tag newTag = tagCommandBuilder.create()
        .setRevision(revision)
        .setName(tagName)
        .execute();
      return Response.created(URI.create(resourceLinks.tag().self(namespace, name, newTag.getName()))).build();
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
        return Response.ok(tagToTagDtoMapper.map(tag, repositoryService.getRepository())).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Error on getting tag from repository.")
          .build();
      }
    }
  }

  @DELETE
  @Path("{tagName}")
  @Produces(VndMediaType.TAG)
  @Operation(summary = "Delete tag", description = "Deletes the tag provided in the path", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success"
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
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("tagName") String tagName) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      RepositoryPermissions.push(repositoryService.getRepository()).check();

      if (tagExists(tagName, repositoryService)) {
        repositoryService.getTagCommand().delete()
          .setName(tagName)
          .execute();
      }

      return Response.noContent().build();
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

  private boolean tagExists(String tagName, RepositoryService repositoryService) throws IOException {
    return getTags(repositoryService)
      .getTagByName(tagName) != null;
  }

}

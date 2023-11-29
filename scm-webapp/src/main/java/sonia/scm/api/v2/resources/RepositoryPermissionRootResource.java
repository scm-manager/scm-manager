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
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.AlreadyExistsException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.util.Optional;
import java.util.function.Predicate;

import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.api.v2.resources.RepositoryPermissionDto.GROUP_PREFIX;

@Slf4j
public class RepositoryPermissionRootResource {

  private RepositoryPermissionDtoToRepositoryPermissionMapper dtoToModelMapper;
  private RepositoryPermissionToRepositoryPermissionDtoMapper modelToDtoMapper;
  private RepositoryPermissionCollectionToDtoMapper repositoryPermissionCollectionToDtoMapper;
  private ResourceLinks resourceLinks;
  private final RepositoryManager manager;

  @Inject
  public RepositoryPermissionRootResource(
    RepositoryPermissionDtoToRepositoryPermissionMapper dtoToModelMapper,
    RepositoryPermissionToRepositoryPermissionDtoMapper modelToDtoMapper,
    RepositoryPermissionCollectionToDtoMapper repositoryPermissionCollectionToDtoMapper,
    ResourceLinks resourceLinks,
    RepositoryManager manager) {
    this.dtoToModelMapper = dtoToModelMapper;
    this.modelToDtoMapper = modelToDtoMapper;
    this.repositoryPermissionCollectionToDtoMapper = repositoryPermissionCollectionToDtoMapper;
    this.resourceLinks = resourceLinks;
    this.manager = manager;
  }

  /**
   * Adds a new permission to the user or group managed by the repository
   *
   * @param permission permission to add
   * @return a web response with the status code 201 and the url to GET the added permission
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.REPOSITORY_PERMISSION)
  @Operation(
    summary = "Create repository-specific permission",
    description = "Adds a new permission to the user or group managed by the repository.",
    tags = {"Repository", "Permissions"},
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.REPOSITORY_PERMISSION,
        schema = @Schema(implementation = UpdateRepositoryPermissionDto.class),
        examples = @ExampleObject(
          name = "Add read permissions for repository and pull requests to manager group.",
          value = "{\n  \"name\":\"manager\",\n  \"verbs\":[\"read\",\"readPullRequest\"],\n  \"groupPermission\":true\n}",
          summary = "Add permissions"
        )
      )
    )
  )
  @ApiResponse(
    responseCode = "201",
    description = "creates",
    headers = @Header(
      name = "Location",
      description = "uri of the created permission",
      schema = @Schema(type = "string")
    )
  )
  @ApiResponse(
    responseCode = "404",
    description = "not found",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "409", description = "conflict")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response create(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid RepositoryPermissionDto permission) {
    log.info("try to add new permission: {}", permission);
    Repository repository = load(namespace, name);
    RepositoryPermissions.permissionWrite(repository).check();
    checkPermissionAlreadyExists(permission, repository);
    repository.addPermission(dtoToModelMapper.map(permission));
    manager.modify(repository);
    String urlPermissionName = modelToDtoMapper.getUrlPermissionName(permission);
    return Response.created(URI.create(resourceLinks.repositoryPermission().self(namespace, name, urlPermissionName))).build();
  }

  /**
   * Get the searched permission with permission name related to a repository
   *
   * @param namespace the repository namespace
   * @param name      the repository name
   * @return the http response with a list of permissionDto objects
   * @throws NotFoundException if the repository does not exists
   */
  @GET
  @Path("{permission-name}")
  @Produces(VndMediaType.REPOSITORY_PERMISSION)
  @Operation(summary = "Get single repository-specific permission", description = "Get the searched permission with permission name related to a repository.", tags = {"Repository", "Permissions"})
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_PERMISSION,
      schema = @Schema(implementation = RepositoryPermissionDto.class)
    )
  )
  @ApiResponse(
    responseCode = "404",
    description = "not found",
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
    )
  )
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("permission-name") String permissionName) {
    Repository repository = load(namespace, name);
    RepositoryPermissions.permissionRead(repository).check();
    return Response.ok(
      repository.getPermissions()
        .stream()
        .filter(filterPermission(permissionName))
        .map(permission -> modelToDtoMapper.map(permission, repository))
        .findFirst()
        .orElseThrow(() -> notFound(entity(RepositoryPermission.class, namespace).in(Repository.class, namespace + "/" + name)))
    ).build();
  }

  /**
   * Get all permissions related to a repository
   *
   * @param namespace the repository namespace
   * @param name      the repository name
   * @return the http response with a list of permissionDto objects
   * @throws NotFoundException if the repository does not exists
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_PERMISSION)
  @Operation(summary = "List of repository-specific permissions", description = "Get all permissions related to a repository.", tags = {"Repository", "Permissions"})
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_PERMISSION,
      schema = @Schema(implementation = RepositoryPermissionDto.class)
    )
  )
  @ApiResponse(
    responseCode = "404",
    description = "not found",
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
    )
  )
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = load(namespace, name);
    RepositoryPermissions.permissionRead(repository).check();
    return Response.ok(repositoryPermissionCollectionToDtoMapper.map(repository)).build();
  }

  /**
   * Update a permission to the user or group managed by the repository
   * ignore the user input for groupPermission and take it from the path parameter (if the group prefix (@) exists it is a group permission)
   *
   * @param permission     permission to modify
   * @param permissionName permission to modify
   * @return a web response with the status code 204
   */
  @PUT
  @Path("{permission-name}")
  @Consumes(VndMediaType.REPOSITORY_PERMISSION)
  @Operation(
    summary = "Update repository-specific permission",
    description = "Update a permission to the user or group managed by the repository.",
    tags = {"Repository", "Permissions"},
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.REPOSITORY_PERMISSION,
        schema = @Schema(implementation = UpdateRepositoryPermissionDto.class),
        examples = @ExampleObject(
          name = "Update permissions of manager group.",
          value = "{\n  \"name\":\"manager\",\n  \"verbs\":[\"read\",\"permissionRead\",\"readPullRequest\"],\n  \"groupPermission\":true\n}",
          summary = "Update a permission"
        )
      )
    ))
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response update(@PathParam("namespace") String namespace,
                         @PathParam("name") String name,
                         @PathParam("permission-name") String permissionName,
                         @Valid RepositoryPermissionDto permission) {
    log.info("try to update the permission with name: {}. the modified permission is: {}", permissionName, permission);
    Repository repository = load(namespace, name);
    RepositoryPermissions.permissionWrite(repository).check();
    String extractedPermissionName = getPermissionName(permissionName);
    if (!isPermissionExist(new RepositoryPermissionDto(extractedPermissionName, isGroupPermission(permissionName)), repository)) {
      throw notFound(entity(RepositoryPermission.class, namespace).in(Repository.class, namespace + "/" + name));
    }
    permission.setGroupPermission(isGroupPermission(permissionName));
    if (!extractedPermissionName.equals(permission.getName())) {
      checkPermissionAlreadyExists(permission, repository);
    }

    RepositoryPermission existingPermission = repository.getPermissions()
      .stream()
      .filter(filterPermission(permissionName))
      .findFirst()
      .orElseThrow(() -> notFound(entity(RepositoryPermission.class, namespace).in(Repository.class, namespace + "/" + name)));
    RepositoryPermission newPermission = dtoToModelMapper.map(permission);
    if (!repository.removePermission(existingPermission)) {
      throw new IllegalStateException(String.format("could not delete modified permission %s from repository %s/%s", existingPermission, namespace, name));
    }
    repository.addPermission(newPermission);
    manager.modify(repository);
    log.info("the permission with name: {} is updated.", permissionName);
    return Response.noContent().build();
  }

  /**
   * Update a permission to the user or group managed by the repository
   *
   * @param permissionName permission to delete
   * @return a web response with the status code 204
   */
  @DELETE
  @Path("{permission-name}")
  @Operation(summary = "Delete repository-specific permission", description = "Delete a permission with the given name.", tags = {"Repository", "Permissions"})
  @ApiResponse(responseCode = "204", description = "delete success or nothing to delete")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response delete(@PathParam("namespace") String namespace,
                         @PathParam("name") String name,
                         @PathParam("permission-name") String permissionName) {
    log.info("try to delete the permission with name: {}.", permissionName);
    Repository repository = load(namespace, name);
    RepositoryPermissions.modify(repository).check();
    repository.getPermissions()
      .stream()
      .filter(filterPermission(permissionName))
      .findFirst()
      .ifPresent(repository::removePermission);
    manager.modify(repository);
    log.info("the permission with name: {} is deleted.", permissionName);
    return Response.noContent().build();
  }

  private Predicate<RepositoryPermission> filterPermission(String name) {
    return permission -> getPermissionName(name).equals(permission.getName())
      &&
      permission.isGroupPermission() == isGroupPermission(name);
  }

  private String getPermissionName(String permissionName) {
    return Optional.of(permissionName)
      .filter(p -> !isGroupPermission(permissionName))
      .orElse(permissionName.substring(1));
  }

  private boolean isGroupPermission(String permissionName) {
    return permissionName.startsWith(GROUP_PREFIX);
  }

  /**
   * check if the actual user is permitted to manage the repository permissions
   * return the repository if the user is permitted
   *
   * @param namespace the repository namespace
   * @param name      the repository name
   * @return the repository if the user is permitted
   * @throws NotFoundException if the repository does not exists
   */
  private Repository load(String namespace, String name) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    return Optional.ofNullable(manager.get(namespaceAndName))
      .orElseThrow(() -> notFound(entity(namespaceAndName)));
  }

  /**
   * check if the permission already exists in the repository
   *
   * @param permission the searched permission
   * @param repository the repository to be inspected
   * @throws AlreadyExistsException if the permission already exists in the repository
   */
  private void checkPermissionAlreadyExists(RepositoryPermissionDto permission, Repository repository) {
    if (isPermissionExist(permission, repository)) {
      throw alreadyExists(entity("Permission", permission.getName()).in(repository));
    }
  }

  private boolean isPermissionExist(RepositoryPermissionDto permission, Repository repository) {
    return repository.getPermissions()
      .stream()
      .anyMatch(p -> p.getName().equals(permission.getName()) && p.isGroupPermission() == permission.isGroupPermission());
  }
}



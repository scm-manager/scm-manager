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

import de.otto.edison.hal.HalRepresentation;
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
import sonia.scm.NotFoundException;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.util.Optional;
import java.util.function.Predicate;

import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.api.v2.resources.RepositoryPermissionDto.GROUP_PREFIX;

@Slf4j
public class NamespacePermissionResource {

  private final RepositoryPermissionDtoToRepositoryPermissionMapper dtoToModelMapper;
  private final RepositoryPermissionToRepositoryPermissionDtoMapper modelToDtoMapper;
  private final RepositoryPermissionCollectionToDtoMapper repositoryPermissionCollectionToDtoMapper;
  private final ResourceLinks resourceLinks;
  private final NamespaceManager manager;

  @Inject
  public NamespacePermissionResource(
    RepositoryPermissionDtoToRepositoryPermissionMapper dtoToModelMapper,
    RepositoryPermissionToRepositoryPermissionDtoMapper modelToDtoMapper,
    RepositoryPermissionCollectionToDtoMapper repositoryPermissionCollectionToDtoMapper,
    ResourceLinks resourceLinks,
    NamespaceManager manager) {
    this.dtoToModelMapper = dtoToModelMapper;
    this.modelToDtoMapper = modelToDtoMapper;
    this.repositoryPermissionCollectionToDtoMapper = repositoryPermissionCollectionToDtoMapper;
    this.resourceLinks = resourceLinks;
    this.manager = manager;
  }

  /**
   * Adds a new namespace permission for the user or group
   *
   * @param permission permission to add
   * @return a web response with the status code 201 and the url to GET the added permission
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.REPOSITORY_PERMISSION)
  @Operation(
    summary = "Create namespace-specific permission",
    description = "Adds a new permission to the namespace for the user or group.",
    tags = {"Namespace", "Permissions"},
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.REPOSITORY_PERMISSION,
        schema = @Schema(implementation = UpdateRepositoryPermissionDto.class),
        examples = @ExampleObject(
          name = "Add read permissions for repositories and pull requests to manager group.",
          value = "{\n  \"name\":\"manager\",\n  \"verbs\":[\"read\",\"readPullRequest\"],\n  \"groupPermission\":true\n}",
          summary = "Add a permission"
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
  public Response create(@PathParam("namespace") String namespaceName, @Valid RepositoryPermissionDto permission) {
    log.info("try to add new permission: {}", permission);
    Namespace namespace = load(namespaceName);
    checkPermissionAlreadyExists(permission, namespace);
    namespace.addPermission(dtoToModelMapper.map(permission));
    manager.modify(namespace);
    String urlPermissionName = modelToDtoMapper.getUrlPermissionName(permission);
    return Response.created(URI.create(resourceLinks.namespacePermission().self(namespaceName, urlPermissionName))).build();
  }

  /**
   * Get the searched permission with permission name related to a namespace
   *
   * @param namespaceName the name of the namespace
   * @return the http response with a list of permissionDto objects
   * @throws NotFoundException if the namespace or the permission does not exists
   */
  @GET
  @Path("{permission-name}")
  @Produces(VndMediaType.REPOSITORY_PERMISSION)
  @Operation(summary = "Get single namespace-specific permission", description = "Get the searched permission with permission name related to a repository.", tags = {"Namespace", "Permissions"})
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
  public RepositoryPermissionDto get(@PathParam("namespace") String namespaceName, @PathParam("permission-name") String permissionName) {
    Namespace namespace = load(namespaceName);
    return
      namespace.getPermissions()
        .stream()
        .filter(filterPermission(permissionName))
        .map(permission -> modelToDtoMapper.map(permission, namespace))
        .findFirst()
        .orElseThrow(() -> notFound(entity(RepositoryPermission.class, permissionName).in(Namespace.class, namespaceName)));
  }

  /**
   * Get all permissions related to a namespace
   *
   * @param namespaceMame the name of the namespace
   * @return the http response with a list of permissionDto objects
   * @throws NotFoundException if the namespace does not exists
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_PERMISSION)
  @Operation(summary = "List of namespace-specific permissions", description = "Get all permissions related to a namespace.", tags = {"Namespace", "Permissions"})
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
  public HalRepresentation getAll(@PathParam("namespace") String namespaceMame) {
    Namespace namespace = load(namespaceMame);
    return repositoryPermissionCollectionToDtoMapper.map(namespace);
  }

  /**
   * Update a permission to the user or group managed by the repository
   * ignore the user input for groupPermission and take it from the path parameter (if the group prefix (@) exists it is a group permission)
   *
   * @param permission     permission to modify
   * @param permissionName permission to modify
   */
  @PUT
  @Path("{permission-name}")
  @Consumes(VndMediaType.REPOSITORY_PERMISSION)
  @Operation(
    summary = "Update namespace-specific permission",
    description = "Update a permission to the user or group managed by the repository.",
    tags = {"Namespace", "Permissions"},
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
    )
  )
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
  public void update(@PathParam("namespace") String namespaceName,
                     @PathParam("permission-name") String permissionName,
                     @Valid RepositoryPermissionDto permission) {
    Namespace namespace = load(namespaceName);
    String extractedPermissionName = getPermissionName(permissionName);
    if (!isPermissionExist(new RepositoryPermissionDto(extractedPermissionName, isGroupPermission(permissionName)), namespace)) {
      throw notFound(entity(RepositoryPermission.class, permissionName).in(Namespace.class, namespaceName));
    }
    permission.setGroupPermission(isGroupPermission(permissionName));
    if (!extractedPermissionName.equals(permission.getName())) {
      checkPermissionAlreadyExists(permission, namespace);
    }

    RepositoryPermission existingPermission = namespace.getPermissions()
      .stream()
      .filter(filterPermission(permissionName))
      .findFirst()
      .orElseThrow(() -> notFound(entity(RepositoryPermission.class, permissionName).in(Namespace.class, namespaceName)));
    RepositoryPermission newPermission = dtoToModelMapper.map(permission);
    if (!namespace.removePermission(existingPermission)) {
      throw new IllegalStateException(String.format("could not delete modified permission %s from namespace %s", existingPermission, namespaceName));
    }
    namespace.addPermission(newPermission);
    manager.modify(namespace);
    log.info("the permission with name: {} is updated to {}.", permissionName, permission);
  }

  /**
   * Update a permission to the user or group managed by the repository
   *
   * @param permissionName permission to delete
   */
  @DELETE
  @Path("{permission-name}")
  @Operation(summary = "Delete namespace-specific permission", description = "Delete a permission with the given name.", tags = {"Namespace", "Permissions"})
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
  public void delete(@PathParam("namespace") String namespaceName,
                     @PathParam("permission-name") String permissionName) {
    log.info("try to delete the permission with name: {}.", permissionName);
    Namespace namespace = load(namespaceName);
    namespace.getPermissions()
      .stream()
      .filter(filterPermission(permissionName))
      .findFirst()
      .ifPresent(permission -> {
        namespace.removePermission(permission);
        manager.modify(namespace);
      });
    log.info("the permission with name: {} is deleted.", permissionName);
  }

  private Predicate<RepositoryPermission> filterPermission(String name) {
    return permission ->
      getPermissionName(name).equals(permission.getName())
        && permission.isGroupPermission() == isGroupPermission(name);
  }

  private String getPermissionName(String permissionName) {
    return Optional.of(permissionName)
      .filter(p -> !isGroupPermission(permissionName))
      .orElse(permissionName.substring(1));
  }

  private boolean isGroupPermission(String permissionName) {
    return permissionName.startsWith(GROUP_PREFIX);
  }

  private Namespace load(String namespaceMame) {
    return manager.get(namespaceMame)
      .orElseThrow(() -> notFound(entity("Namespace", namespaceMame)));
  }

  private void checkPermissionAlreadyExists(RepositoryPermissionDto permission, Namespace namespace) {
    if (isPermissionExist(permission, namespace)) {
      throw alreadyExists(entity("Permission", permission.getName()).in(Namespace.class, namespace.getNamespace()));
    }
  }

  private boolean isPermissionExist(RepositoryPermissionDto permission, Namespace namespace) {
    return namespace.getPermissions()
      .stream()
      .anyMatch(p -> p.getName().equals(permission.getName()) && p.isGroupPermission() == permission.isGroupPermission());
  }

}

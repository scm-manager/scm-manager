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
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class RepositoryResource {

  private final RepositoryToRepositoryDtoMapper repositoryToDtoMapper;
  private final RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;

  private final RepositoryManager manager;
  private final SingleResourceManagerAdapter<Repository, RepositoryDto> adapter;
  private final Provider<TagRootResource> tagRootResource;
  private final Provider<BranchRootResource> branchRootResource;
  private final Provider<ChangesetRootResource> changesetRootResource;
  private final Provider<SourceRootResource> sourceRootResource;
  private final Provider<ContentResource> contentResource;
  private final Provider<RepositoryPermissionRootResource> permissionRootResource;
  private final Provider<DiffRootResource> diffRootResource;
  private final Provider<ModificationsRootResource> modificationsRootResource;
  private final Provider<FileHistoryRootResource> fileHistoryRootResource;
  private final Provider<IncomingRootResource> incomingRootResource;

  @Inject
  public RepositoryResource(
    RepositoryToRepositoryDtoMapper repositoryToDtoMapper,
    RepositoryDtoToRepositoryMapper dtoToRepositoryMapper, RepositoryManager manager,
    Provider<TagRootResource> tagRootResource,
    Provider<BranchRootResource> branchRootResource,
    Provider<ChangesetRootResource> changesetRootResource,
    Provider<SourceRootResource> sourceRootResource, Provider<ContentResource> contentResource,
    Provider<RepositoryPermissionRootResource> permissionRootResource,
    Provider<DiffRootResource> diffRootResource,
    Provider<ModificationsRootResource> modificationsRootResource,
    Provider<FileHistoryRootResource> fileHistoryRootResource,
    Provider<IncomingRootResource> incomingRootResource
  ) {
    this.dtoToRepositoryMapper = dtoToRepositoryMapper;
    this.manager = manager;
    this.repositoryToDtoMapper = repositoryToDtoMapper;
    this.adapter = new SingleResourceManagerAdapter<>(manager, Repository.class);
    this.tagRootResource = tagRootResource;
    this.branchRootResource = branchRootResource;
    this.changesetRootResource = changesetRootResource;
    this.sourceRootResource = sourceRootResource;
    this.contentResource = contentResource;
    this.permissionRootResource = permissionRootResource;
    this.diffRootResource = diffRootResource;
    this.modificationsRootResource = modificationsRootResource;
    this.fileHistoryRootResource = fileHistoryRootResource;
    this.incomingRootResource = incomingRootResource;

  }

  /**
   * Returns a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository
   * @param name the name of the repository
   *
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY)
  @Operation(summary = "Get single repository", description = "Returns the repository for the given namespace and name.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY,
      schema = @Schema(implementation = RepositoryDto.class)
    )
  )
  @ApiResponse(
    responseCode = "401",
    description = "not authenticated / invalid credentials"
  )
  @ApiResponse(
    responseCode = "403",
    description = "not authorized, the current user has no privileges to read the repository"
  )
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified name available in the namespace",
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
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name){
    return adapter.get(loadBy(namespace, name), repositoryToDtoMapper::map);
  }

  /**
   * Deletes a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository to delete
   * @param name the name of the repository to delete
   *
   */
  @DELETE
  @Path("")
  @Operation(summary = "Delete repository", description = "Deletes the repository with the given namespace and name.", tags = "Repository")
  @ApiResponse(responseCode = "204", description = "delete success or nothing to delete")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository\" privilege")
  @ApiResponse(responseCode = "500", description = "internal server error")
  public Response delete(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return adapter.delete(loadBy(namespace, name));
  }

  /**
   * Modifies the given repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository to be modified
   * @param name the name of the repository to be modified
   * @param repository repository object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.REPOSITORY)
  @Operation(summary = "Update repository", description = "Modifies the repository for the given namespace and name.", tags = "Repository")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body, e.g. illegal change of namespace or name")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
  public Response update(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid RepositoryDto repository) {
    return adapter.update(
      loadBy(namespace, name),
      existing -> processUpdate(repository, existing),
      nameAndNamespaceStaysTheSame(namespace, name),
      r -> r.getNamespaceAndName().logString()
    );
  }

  private Repository processUpdate(RepositoryDto repositoryDto, Repository existing) {
    Repository changedRepository = dtoToRepositoryMapper.map(repositoryDto, existing.getId());
    changedRepository.setPermissions(existing.getPermissions());
    return changedRepository;
  }

  @Path("tags/")
  public TagRootResource tags() {
    return tagRootResource.get();
  }

  @Path("diff/")
  public DiffRootResource diff() {
    return diffRootResource.get();
  }

  @Path("branches/")
  public BranchRootResource branches() {
    return branchRootResource.get();
  }

  @Path("changesets/")
  public ChangesetRootResource changesets() {
    return changesetRootResource.get();
  }

  @Path("history/")
  public FileHistoryRootResource history() {
    return fileHistoryRootResource.get();
  }

  @Path("sources/")
  public SourceRootResource sources() {
    return sourceRootResource.get();
  }

  @Path("content/")
  public ContentResource content() {
    return contentResource.get();
  }

  @Path("permissions/")
  public RepositoryPermissionRootResource permissions() {
    return permissionRootResource.get();
  }

  @Path("modifications/")
  public ModificationsRootResource modifications() {
    return modificationsRootResource.get();
  }

  @Path("incoming/")
  public IncomingRootResource incoming() {
    return incomingRootResource.get();
  }

  private Supplier<Repository> loadBy(String namespace, String name) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    return () -> Optional.ofNullable(manager.get(namespaceAndName)).orElseThrow(() -> notFound(entity(namespaceAndName)));
  }

  private Predicate<Repository> nameAndNamespaceStaysTheSame(String namespace, String name) {
    return changed -> name.equals(changed.getName()) && namespace.equals(changed.getNamespace());
  }
}

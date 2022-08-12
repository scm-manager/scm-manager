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
import sonia.scm.HandlerEventType;
import sonia.scm.event.ScmEventBus;
import sonia.scm.notifications.Notification;
import sonia.scm.notifications.Type;
import sonia.scm.repository.HealthCheckService;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.search.ReindexRepositoryEvent;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
  private final RepositoryBasedResourceProvider resourceProvider;
  private final HealthCheckService healthCheckService;

  @Inject
  public RepositoryResource(
    RepositoryToRepositoryDtoMapper repositoryToDtoMapper,
    RepositoryDtoToRepositoryMapper dtoToRepositoryMapper, RepositoryManager manager,
    RepositoryBasedResourceProvider resourceProvider, HealthCheckService healthCheckService) {
    this.dtoToRepositoryMapper = dtoToRepositoryMapper;
    this.manager = manager;
    this.repositoryToDtoMapper = repositoryToDtoMapper;
    this.adapter = new SingleResourceManagerAdapter<>(manager, Repository.class);
    this.resourceProvider = resourceProvider;
    this.healthCheckService = healthCheckService;
  }

  /**
   * Returns a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository
   * @param name      the name of the repository
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
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    return adapter.get(loadBy(namespace, name), repositoryToDtoMapper::map);
  }

  /**
   * Deletes a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository to delete
   * @param name      the name of the repository to delete
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
   * @param namespace  the namespace of the repository to be modified
   * @param name       the name of the repository to be modified
   * @param repository repository object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.REPOSITORY)
  @Operation(
    summary = "Update repository",
    description = "Modifies the repository for the given namespace and name.",
    tags = "Repository",
    requestBody = @RequestBody(
      content = @Content(
        mediaType = VndMediaType.REPOSITORY,
        schema = @Schema(implementation = UpdateRepositoryDto.class),
        examples = @ExampleObject(
          name = "Update repository description.",
          value = "{\n  \"namespace\":\"scmadmin\",\n  \"name\":\"scm-manager\",\n  \"description\":\"The easiest way to share and manage your Git, Mercurial and Subversion repositories.\",\n  \"type\":\"git\",\n  \"lastModified\":\"2020-06-05T14:42:49.000Z\"\n}",
          summary = "Update a repository"
        )
      )
    )
  )
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

  /**
   * Renames the given repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository to be modified
   * @param name      the name of the repository to be modified
   * @param renameDto renameDto object to modify
   */
  @POST
  @Path("rename")
  @Consumes(VndMediaType.REPOSITORY)
  @Operation(summary = "Rename repository", description = "Renames the repository for the given namespace and name.", tags = "Repository")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid body, e.g. illegal change of namespace or name")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository:renameDto\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
  public void rename(@PathParam("namespace") String namespace, @PathParam("name") String name, @Valid RepositoryRenameDto renameDto) {
    Repository repository = loadBy(namespace, name).get();
    manager.rename(repository, renameDto.getNamespace(), renameDto.getName());
  }

  /**
   * Marks the given repository as "archived".
   *
   * @param namespace the namespace of the repository to be marked
   * @param name      the name of the repository to be marked
   */
  @POST
  @Path("archive")
  @Operation(summary = "Mark repository as \"archived\"", description = "Marks the repository as \"archived\".", tags = "Repository")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid request, e.g. when the repository already is marked as archived")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository:archive\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
  public void archive(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = loadBy(namespace, name).get();
    manager.archive(repository);
  }

  /**
   * Marks the given repository as not "archived".
   *
   * @param namespace the namespace of the repository to remove the mark from
   * @param name      the name of the repository to remove the mark from
   */
  @POST
  @Path("unarchive")
  @Operation(summary = "Mark repository as \"not archived\"", description = "Removes the \"archived\" mark from the repository.", tags = "Repository")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "400", description = "invalid request, e.g. when the repository is not marked as archived")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository:archive\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
  public void unarchive(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = loadBy(namespace, name).get();
    manager.unarchive(repository);
  }

  @POST
  @Path("runHealthCheck")
  @Operation(summary = "Check health of repository", description = "Starts a full health check for the repository.", tags = "Repository")
  @ApiResponse(responseCode = "204", description = "check started")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository:healthCheck\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
  public void runHealthCheck(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = loadBy(namespace, name).get();
    healthCheckService.fullCheck(repository);
  }

  @POST
  @Path("reindex")
  @Operation(summary = "Manually reindex repository", description = "Asynchronously update search indices for repository", tags = "Repository")
  @ApiResponse(responseCode = "204", description = "event submitted")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository:reindex\" privilege")
  @ApiResponse(
    responseCode = "404",
    description = "not found, no repository with the specified namespace and name available",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  @ApiResponse(responseCode = "500", description = "internal server error")
  public void reindex(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = loadBy(namespace, name).get();
    RepositoryPermissions.custom("*", repository).check();
    ScmEventBus.getInstance().post(new ReindexRepositoryEvent(repository));
  }

  private Repository processUpdate(RepositoryDto repositoryDto, Repository existing) {
    Repository changedRepository = dtoToRepositoryMapper.map(repositoryDto, existing.getId());
    changedRepository.setPermissions(existing.getPermissions());
    return changedRepository;
  }

  @Path("tags/")
  public TagRootResource tags() {
    return resourceProvider.getTagRootResource();
  }

  @Path("diff/")
  public DiffRootResource diff() {
    return resourceProvider.getDiffRootResource();
  }

  @Path("branches/")
  public BranchRootResource branches() {
    return resourceProvider.getBranchRootResource();
  }

  @Path("branch-details/")
  public BranchDetailsResource branchDetails() {
    return resourceProvider.getBranchDetailsResource();
  }

  @Path("changesets/")
  public ChangesetRootResource changesets() {
    return resourceProvider.getChangesetRootResource();
  }

  @Path("history/")
  public FileHistoryRootResource history() {
    return resourceProvider.getFileHistoryRootResource();
  }

  @Path("sources/")
  public SourceRootResource sources() {
    return resourceProvider.getSourceRootResource();
  }

  @Path("content/")
  public ContentResource content() {
    return resourceProvider.getContentResource();
  }

  @Path("permissions/")
  public RepositoryPermissionRootResource permissions() {
    return resourceProvider.getPermissionRootResource();
  }

  @Path("modifications/")
  public ModificationsRootResource modifications() {
    return resourceProvider.getModificationsRootResource();
  }

  @Path("incoming/")
  public IncomingRootResource incoming() {
    return resourceProvider.getIncomingRootResource();
  }

  @Path("annotate/")
  public AnnotateResource annotate() {
    return resourceProvider.getAnnotateResource();
  }

  @Path("export/")
  public RepositoryExportResource export() {
    return resourceProvider.getRepositoryExportResource();
  }

  @Path("paths/")
  public RepositoryPathsResource paths() {
    return resourceProvider.getRepositoryPathResource();
  }

  private Supplier<Repository> loadBy(String namespace, String name) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    return () -> Optional.ofNullable(manager.get(namespaceAndName)).orElseThrow(() -> notFound(entity(namespaceAndName)));
  }

  private Predicate<Repository> nameAndNamespaceStaysTheSame(String namespace, String name) {
    return changed -> name.equals(changed.getName()) && namespace.equals(changed.getNamespace());
  }

}

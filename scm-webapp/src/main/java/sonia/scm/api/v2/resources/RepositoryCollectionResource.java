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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.shiro.SecurityUtils;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryInitializer;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;
import sonia.scm.user.User;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.singletonList;

public class RepositoryCollectionResource {

  private static final int DEFAULT_PAGE_SIZE = 10;

  private final CollectionResourceManagerAdapter<Repository, RepositoryDto> adapter;
  private final RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper;
  private final RepositoryDtoToRepositoryMapper dtoToRepositoryMapper;
  private final ResourceLinks resourceLinks;
  private final RepositoryInitializer repositoryInitializer;

  @Inject
  public RepositoryCollectionResource(RepositoryManager manager, RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper, RepositoryDtoToRepositoryMapper dtoToRepositoryMapper, ResourceLinks resourceLinks, RepositoryInitializer repositoryInitializer) {
    this.adapter = new CollectionResourceManagerAdapter<>(manager, Repository.class);
    this.repositoryCollectionToDtoMapper = repositoryCollectionToDtoMapper;
    this.dtoToRepositoryMapper = dtoToRepositoryMapper;
    this.resourceLinks = resourceLinks;
    this.repositoryInitializer = repositoryInitializer;
  }

  /**
   * Returns all repositories for a given page number with a given page size (default page size is {@value DEFAULT_PAGE_SIZE}).
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param page     the number of the requested page
   * @param pageSize the page size (default page size is {@value DEFAULT_PAGE_SIZE})
   * @param sortBy   sort parameter (if empty - undefined sorting)
   * @param desc     sort direction desc or asc
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_COLLECTION)
  @Operation(summary = "List of repositories", description = "Returns all repositories for a given page number with a given page size.", tags = "Repository")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = VndMediaType.REPOSITORY_COLLECTION,
      schema = @Schema(implementation = CollectionDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
                         @DefaultValue("" + DEFAULT_PAGE_SIZE) @QueryParam("pageSize") int pageSize,
                         @QueryParam("sortBy") String sortBy,
                         @DefaultValue("false") @QueryParam("desc") boolean desc,
                         @DefaultValue("") @QueryParam("q") String search
  ) {
    return adapter.getAll(page, pageSize, createSearchPredicate(search), sortBy, desc,
      pageResult -> repositoryCollectionToDtoMapper.map(page, pageSize, pageResult));
  }

  /**
   * Creates a new repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege. The namespace of the given repository will
   * be ignored and set by the configured namespace strategy.
   *
   * @param repository The repository to be created.
   * @return A response with the link to the new repository (if created successfully).
   */
  @POST
  @Path("")
  @Consumes(VndMediaType.REPOSITORY)
  @Operation(summary = "Create repository", description = "Creates a new repository.", tags = "Repository")
  @ApiResponse(
    responseCode = "201",
    description = "create success",
    headers = @Header(
      name = "Location",
      description = "uri to the created repository",
      schema = @Schema(type = "string")
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"repository\" privilege")
  @ApiResponse(responseCode = "409", description = "conflict, a repository with this name already exists")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    ))
  public Response create(@Valid RepositoryDto repository, @QueryParam("initialize") boolean initialize) {
    AtomicReference<Repository> reference = new AtomicReference<>();
    Response response = adapter.create(repository,
      () -> createModelObjectFromDto(repository),
      r -> {
        reference.set(r);
        return resourceLinks.repository().self(r.getNamespace(), r.getName());
      });
    if (initialize) {
      repositoryInitializer.initialize(reference.get());
    }
    return response;
  }

  private Repository createModelObjectFromDto(@Valid RepositoryDto repositoryDto) {
    Repository repository = dtoToRepositoryMapper.map(repositoryDto, null);
    repository.setPermissions(singletonList(new RepositoryPermission(currentUser(), "OWNER", false)));
    return repository;
  }

  private String currentUser() {
    return SecurityUtils.getSubject().getPrincipals().oneByType(User.class).getName();
  }

  private Predicate<Repository> createSearchPredicate(String search) {
    if (isNullOrEmpty(search)) {
      return user -> true;
    }
    SearchRequest searchRequest = new SearchRequest(search, true);
    return repository -> SearchUtil.matchesOne(searchRequest, repository.getName(), repository.getNamespace(), repository.getDescription());
  }
}

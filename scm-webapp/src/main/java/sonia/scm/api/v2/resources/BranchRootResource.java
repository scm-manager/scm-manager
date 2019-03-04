package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.NotFoundException;
import sonia.scm.PageResult;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Branches;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.CommandNotSupportedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class BranchRootResource {

  private final RepositoryServiceFactory serviceFactory;
  private final BranchToBranchDtoMapper branchToDtoMapper;
  private final BranchCollectionToDtoMapper branchCollectionToDtoMapper;

  private final BranchChangesetCollectionToDtoMapper branchChangesetCollectionToDtoMapper;

  @Inject
  public BranchRootResource(RepositoryServiceFactory serviceFactory, BranchToBranchDtoMapper branchToDtoMapper, BranchCollectionToDtoMapper branchCollectionToDtoMapper, BranchChangesetCollectionToDtoMapper changesetCollectionToDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.branchToDtoMapper = branchToDtoMapper;
    this.branchCollectionToDtoMapper = branchCollectionToDtoMapper;
    this.branchChangesetCollectionToDtoMapper = changesetCollectionToDtoMapper;
  }

  /**
   * Returns a branch for a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace  the namespace of the repository
   * @param name       the name of the repository
   * @param branchName the name of the branch
   */
  @GET
  @Path("{branch}")
  @Produces(VndMediaType.BRANCH)
  @TypeHint(BranchDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "branches not supported for given repository"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the branch"),
    @ResponseCode(code = 404, condition = "not found, no branch with the specified name for the repository available or repository not found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("branch") String branchName) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Branches branches = repositoryService.getBranchesCommand().getBranches();
      return branches.getBranches()
        .stream()
        .filter(branch -> branchName.equals(branch.getName()))
        .findFirst()
        .map(branch -> branchToDtoMapper.map(branch, new NamespaceAndName(namespace, name)))
        .map(Response::ok)
        .orElse(Response.status(Response.Status.NOT_FOUND))
        .build();
    } catch (CommandNotSupportedException ex) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Path("{branch}/changesets/")
  @GET
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the changeset"),
    @ResponseCode(code = 404, condition = "not found, no changesets available in the repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.CHANGESET_COLLECTION)
  @TypeHint(CollectionDto.class)
  public Response history(@PathParam("namespace") String namespace,
                          @PathParam("name") String name,
                          @PathParam("branch") String branchName,
                          @DefaultValue("0") @QueryParam("page") int page,
                          @DefaultValue("10") @QueryParam("pageSize") int pageSize) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      boolean branchExists = repositoryService.getBranchesCommand()
        .getBranches()
        .getBranches()
        .stream()
        .anyMatch(branch -> branchName.equals(branch.getName()));
      if (!branchExists){
        throw notFound(entity(Branch.class, branchName).in(Repository.class, namespace + "/" + name));
      }
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      ChangesetPagingResult changesets = new PagedLogCommandBuilder(repositoryService)
        .page(page)
        .pageSize(pageSize)
        .create()
        .setBranch(branchName)
        .getChangesets();
      if (changesets != null && changesets.getChangesets() != null) {
        PageResult<Changeset> pageResult = new PageResult<>(changesets.getChangesets(), changesets.getTotal());
        return Response.ok(branchChangesetCollectionToDtoMapper.map(page, pageSize, pageResult, repository, branchName)).build();
      } else {
        return Response.ok().build();
      }
    }
  }

  /**
   * Returns the branches for a repository.
   *
   * <strong>Note:</strong> This method requires "repository" privilege.
   *
   * @param namespace the namespace of the repository
   * @param name      the name of the repository
   */
  @GET
  @Path("")
  @Produces(VndMediaType.BRANCH_COLLECTION)
  @TypeHint(CollectionDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 400, condition = "branches not supported for given repository"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"group\" privilege"),
    @ResponseCode(code = 404, condition = "not found, no repository found for the given namespace and name"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Branches branches = repositoryService.getBranchesCommand().getBranches();
      return Response.ok(branchCollectionToDtoMapper.map(namespace, name, branches.getBranches())).build();
    } catch (CommandNotSupportedException ex) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }
}

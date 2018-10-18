package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
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


@Slf4j
public class ChangesetRootResource {

  private final RepositoryServiceFactory serviceFactory;

  private final ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper;

  private final ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper;

  @Inject
  public ChangesetRootResource(RepositoryServiceFactory serviceFactory, ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper, ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.changesetCollectionToDtoMapper = changesetCollectionToDtoMapper;
    this.changesetToChangesetDtoMapper = changesetToChangesetDtoMapper;
  }

  @GET
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the changeset"),
    @ResponseCode(code = 404, condition = "not found, no changesets available in the repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.CHANGESET_COLLECTION)
  @TypeHint(CollectionDto.class)
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name, @DefaultValue("0") @QueryParam("page") int page,
                         @DefaultValue("10") @QueryParam("pageSize") int pageSize) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      ChangesetPagingResult changesets = new PagedLogCommandBuilder(repositoryService)
        .page(page)
        .pageSize(pageSize)
        .create()
        .getChangesets();
      if (changesets != null && changesets.getChangesets() != null) {
        PageResult<Changeset> pageResult = new PageResult<>(changesets.getChangesets(), changesets.getTotal());
        if (changesets.getBranchName() != null) {
          return Response.ok(changesetCollectionToDtoMapper.map(page, pageSize, pageResult, repository, changesets.getBranchName())).build();
        } else {
          return Response.ok(changesetCollectionToDtoMapper.map(page, pageSize, pageResult, repository)).build();
        }
      } else {
        return Response.ok().build();
      }
    }
  }

  @GET
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the changeset"),
    @ResponseCode(code = 404, condition = "not found, no changeset with the specified id is available in the repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.CHANGESET)
  @TypeHint(ChangesetDto.class)
  @Path("{id}")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("id") String id) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      ChangesetPagingResult changesets = repositoryService.getLogCommand()
        .setStartChangeset(id)
        .setEndChangeset(id)
        .getChangesets();
      if (changesets != null && changesets.getChangesets() != null && changesets.getChangesets().size() == 1) {
        return Response.ok(changesetToChangesetDtoMapper.map(changesets.getChangesets().get(0), repository)).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    }
  }
}

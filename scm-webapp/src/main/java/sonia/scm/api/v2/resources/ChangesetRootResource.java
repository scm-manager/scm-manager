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
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryNotFoundException;
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

  @Inject
  public ChangesetRootResource(RepositoryServiceFactory serviceFactory, ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.changesetCollectionToDtoMapper = changesetCollectionToDtoMapper;
  }

  @GET
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the changeset"),
    @ResponseCode(code = 404, condition = "not found, no changeset with the specified name available in the repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.CHANGESET_COLLECTION)
  @TypeHint(CollectionDto.class)
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name, @DefaultValue("0") @QueryParam("page") int page,
                         @DefaultValue("10") @QueryParam("pageSize") int pageSize) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      ChangesetPagingResult changesets;
      Repository repository = repositoryService.getRepository();
      changesets = repositoryService.getLogCommand()
        .setPagingStart(page)
        .setPagingLimit(pageSize)
        .getChangesets();
      if (changesets != null) {
        PageResult<Changeset> pageResult = new PageResult<>(changesets.getChangesets(), changesets.getTotal());
        return Response.ok(changesetCollectionToDtoMapper.map(page, pageSize, pageResult, repository)).build();
      } else {
        return Response.ok().build();
      }
    } catch (RepositoryNotFoundException e) {
      log.debug("Not found in repository {}/{}", namespace, name, e);
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (RepositoryException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Response.ok().build();
  }

  @GET
  @Path("{id}")
  public Response get(@PathParam("id") String id) {
    throw new UnsupportedOperationException();
  }
}

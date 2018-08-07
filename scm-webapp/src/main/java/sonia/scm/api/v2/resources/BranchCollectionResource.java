package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.Branches;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.api.CommandNotSupportedException;
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

public class BranchCollectionResource {

  private final RepositoryServiceFactory servicefactory;
  private final BranchCollectionToDtoMapper branchCollectionToDtoMapper;

  @Inject
  public BranchCollectionResource(RepositoryServiceFactory servicefactory, BranchCollectionToDtoMapper branchCollectionToDtoMapper) {
    this.servicefactory = servicefactory;
    this.branchCollectionToDtoMapper = branchCollectionToDtoMapper;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.BRANCH_COLLECTION)
  @TypeHint(CollectionDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"group\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name) throws IOException, RepositoryException {
    try (RepositoryService repositoryService = servicefactory.create(new NamespaceAndName(namespace, name))) {
      Branches branches = repositoryService.getBranchesCommand().getBranches();
      return Response.ok(branchCollectionToDtoMapper.map(namespace, name, branches.getBranches())).build();
    } catch (CommandNotSupportedException ex) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    } catch (RepositoryNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }
}

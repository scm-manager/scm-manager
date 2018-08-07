package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.Branches;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.api.CommandNotSupportedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.IOUtil;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class BranchResource {

  private final RepositoryManager manager;
  private final RepositoryServiceFactory servicefactory;
  private final BranchToBranchDtoMapper branchToDtoMapper;

  @Inject
  public BranchResource(RepositoryManager manager, RepositoryServiceFactory servicefactory, BranchToBranchDtoMapper branchToDtoMapper) {
    this.manager = manager;
    this.servicefactory = servicefactory;
    this.branchToDtoMapper = branchToDtoMapper;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.BRANCH)
  @TypeHint(BranchDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the branch"),
    @ResponseCode(code = 404, condition = "not found, no branch with the specified name for the repository available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("branch") String branchName) throws IOException, RepositoryException {
    RepositoryService repositoryService;
    try {
      repositoryService = servicefactory.create(new NamespaceAndName(namespace, name));
    } catch (RepositoryNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    try {
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
    } finally {
      IOUtil.close(repositoryService);
    }
  }
}

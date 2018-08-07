package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class BranchResource {

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
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("branch") String branch) {
    return null;
  }
}

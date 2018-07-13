package sonia.scm.api.v2.resources;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

public class BranchCollectionResource {
  @GET
  @Path("")
  public Response getAll(@DefaultValue("0") @QueryParam("page") int page,
    @DefaultValue("10") @QueryParam("pageSize") int pageSize,
    @QueryParam("sortBy") String sortBy,
    @DefaultValue("false") @QueryParam("desc") boolean desc) {
    throw new UnsupportedOperationException();
  }
}

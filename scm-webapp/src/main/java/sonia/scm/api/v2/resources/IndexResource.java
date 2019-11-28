package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path(IndexResource.INDEX_PATH_V2)
@AllowAnonymousAccess
public class IndexResource {
  public static final String INDEX_PATH_V2 = "v2/";

  private final IndexDtoGenerator indexDtoGenerator;

  @Inject
  public IndexResource(IndexDtoGenerator indexDtoGenerator) {
    this.indexDtoGenerator = indexDtoGenerator;
  }

  @GET
  @Path("")
  @Produces(VndMediaType.INDEX)
  @TypeHint(IndexDto.class)
  public IndexDto getIndex() {
    return indexDtoGenerator.generate();
  }
}

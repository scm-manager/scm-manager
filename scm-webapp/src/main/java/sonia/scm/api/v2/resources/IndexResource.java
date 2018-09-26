package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path(IndexResource.INDEX_PATH_V2)
public class IndexResource {
  public static final String INDEX_PATH_V2 = "v2/";

  private final IndexDtoGenerator indexDtoGenerator;

  @Inject
  public IndexResource(IndexDtoGenerator indexDtoGenerator) {
    this.indexDtoGenerator = indexDtoGenerator;
  }

  @GET
  public IndexDto getIndex() {
    return indexDtoGenerator.generate();
  }
}

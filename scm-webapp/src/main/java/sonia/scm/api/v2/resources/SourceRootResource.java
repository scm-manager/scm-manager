package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

public class SourceRootResource {

  private final Provider<SourceCollectionResource> sourceCollectionResource;

  @Inject
  public SourceRootResource(Provider<SourceCollectionResource> sourceCollectionResource) {
    this.sourceCollectionResource = sourceCollectionResource;
  }

  @Path("")
  public SourceCollectionResource getSourceCollectionResource() {
    return sourceCollectionResource.get();
  }
}

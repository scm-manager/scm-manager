package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

public class TagRootResource {

  private final Provider<TagCollectionResource> tagCollectionResource;

  @Inject
  public TagRootResource(Provider<TagCollectionResource> tagCollectionResource) {
    this.tagCollectionResource = tagCollectionResource;
  }

  @Path("")
  public TagCollectionResource getTagCollectionResource() {
    return tagCollectionResource.get();
  }
}

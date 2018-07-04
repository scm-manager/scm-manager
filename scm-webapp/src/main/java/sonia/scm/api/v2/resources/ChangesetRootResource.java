package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

public class ChangesetRootResource {

  private final Provider<ChangesetCollectionResource> changesetCollectionResource;

  @Inject
  public ChangesetRootResource(Provider<ChangesetCollectionResource> changesetCollectionResource) {
    this.changesetCollectionResource = changesetCollectionResource;
  }

  @Path("")
  public ChangesetCollectionResource getChangesetCollectionResource() {
    return changesetCollectionResource.get();
  }
}

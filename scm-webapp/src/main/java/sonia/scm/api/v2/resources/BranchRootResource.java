package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

public class BranchRootResource {

  private final Provider<BranchCollectionResource> branchCollectionResource;

  @Inject
  public BranchRootResource(Provider<BranchCollectionResource> branchCollectionResource) {
    this.branchCollectionResource = branchCollectionResource;
  }

  @Path("")
  public BranchCollectionResource getBranchCollectionResource() {
    return branchCollectionResource.get();
  }
}

package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

public class BranchRootResource {

  private final Provider<BranchCollectionResource> branchCollectionResource;
  private final Provider<BranchResource> branchResource;

  @Inject
  public BranchRootResource(Provider<BranchCollectionResource> branchCollectionResource, Provider<BranchResource> branchResource) {
    this.branchCollectionResource = branchCollectionResource;
    this.branchResource = branchResource;
  }

  @Path("{branch}")
  public BranchResource getBranchResource() {
    return branchResource.get();
  }

  @Path("")
  public BranchCollectionResource getBranchCollectionResource() {
    return branchCollectionResource.get();
  }
}

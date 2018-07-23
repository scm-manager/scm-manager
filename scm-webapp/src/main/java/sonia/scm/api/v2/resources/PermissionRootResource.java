package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

public class PermissionRootResource {

  private final Provider<PermissionCollectionResource> permissionCollectionResource;

  @Inject
  public PermissionRootResource(Provider<PermissionCollectionResource> permissionCollectionResource) {
    this.permissionCollectionResource = permissionCollectionResource;
  }

  @Path("")
  public PermissionCollectionResource getPermissionCollectionResource() {
    return permissionCollectionResource.get();
  }
}

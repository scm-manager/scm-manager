package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

@Path(UserRootResource.USERS_PATH_V2)
public class UserRootResource {

  public static final String USERS_PATH_V2 = "v2/users/";
  private final Provider<UserCollectionResource> userCollectionResource;
  private final Provider<UserResource> userResource;

  @Inject
  public UserRootResource(Provider<UserCollectionResource> userCollectionResource,
                          Provider<UserResource> userResource) {
    this.userCollectionResource = userCollectionResource;
    this.userResource = userResource;
  }

  @Path("")
  public UserCollectionResource getUserCollectionResource() {
    return userCollectionResource.get();
  }

  @Path("{id}")
  public UserResource getUserResource() {
    return userResource.get();
  }
}

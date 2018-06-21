package sonia.scm.api.v2.resources;

import com.google.inject.Inject;

import javax.ws.rs.Path;

@Path(UserRootResource.USERS_PATH_V2)
public class UserRootResource {

  public static final String USERS_PATH_V2 = "v2/users/";
  private final UserCollectionResource userCollectionResource;
  private final UserResource userResource;

  @Inject
  public UserRootResource(UserCollectionResource userCollectionResource, UserResource userResource) {
    this.userCollectionResource = userCollectionResource;
    this.userResource = userResource;
  }

  @Path("")
  public UserCollectionResource getUserCollectionResource() {
    return userCollectionResource;
  }

  @Path("{id}")
  public UserResource getUserResource() {
    return userResource;
  }
}

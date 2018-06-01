package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.Path;

@Singleton
@Path(UserV2Resource.USERS_PATH_V2)
public class UserV2Resource {

  public static final String USERS_PATH_V2 = "v2/users/";
  private final UserCollectionResource userCollectionResource;
  private final UserSubResource userSubResource;

  @Inject
  public UserV2Resource(UserCollectionResource userCollectionResource, UserSubResource userSubResource) {
    this.userCollectionResource = userCollectionResource;
    this.userSubResource = userSubResource;
  }

  @Path("")
  public UserCollectionResource getUserCollectionResource() {
    return userCollectionResource;
  }

  @Path("{id}")
  public UserSubResource getUserSubResource() {
    return userSubResource;
  }
}

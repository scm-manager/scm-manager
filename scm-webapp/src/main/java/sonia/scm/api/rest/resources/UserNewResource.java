package sonia.scm.api.rest.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.Path;

@Singleton
@Path("usersnew")
public class UserNewResource {

  private final UserCollectionResource userCollectionResource;
  private final UserSubResource userSubResource;

  @Inject
  public UserNewResource(UserCollectionResource userCollectionResource, UserSubResource userSubResource) {
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

package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

/**
 * RESTful Web Service Resource to manage groups and their members.
 */
@Path(GroupRootResource.GROUPS_PATH_V2)
public class GroupRootResource {

  static final String GROUPS_PATH_V2 = "v2/groups/";

  private final Provider<GroupCollectionResource> groupCollectionResource;
  private final Provider<GroupResource> groupResource;

  @Inject
  public GroupRootResource(Provider<GroupCollectionResource> groupCollectionResource,
    Provider<GroupResource> groupResource) {
    this.groupCollectionResource = groupCollectionResource;
    this.groupResource = groupResource;
  }

  @Path("")
  public GroupCollectionResource getGroupCollectionResource() {
    return groupCollectionResource.get();
  }

  @Path("{id}")
  public GroupResource getGroupResource() {
    return groupResource.get();
  }
}

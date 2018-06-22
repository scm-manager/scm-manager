package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path(GroupRootResource.GROUPS_PATH_V2)
public class GroupRootResource {

  public static final String GROUPS_PATH_V2 = "v2/groups/";

  private final GroupCollectionResource groupCollectionResource;
  private final GroupResource groupResource;

  @Inject
  public GroupRootResource(GroupCollectionResource groupCollectionResource, GroupResource groupResource) {
    this.groupCollectionResource = groupCollectionResource;
    this.groupResource = groupResource;
  }

  @Path("")
  public GroupCollectionResource getGroupCollectionResource() {
    return groupCollectionResource;
  }

  @Path("{id}")
  public GroupResource getGroupResource() {
    return groupResource;
  }
}

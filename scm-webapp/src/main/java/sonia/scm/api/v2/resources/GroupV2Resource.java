package sonia.scm.api.v2.resources;

import com.google.inject.Inject;

import javax.ws.rs.Path;

@Path(GroupV2Resource.GROUPS_PATH_V2)
public class GroupV2Resource {

  public static final String GROUPS_PATH_V2 = "v2/groups/";

  private final GroupCollectionResource groupCollectionResource;
  private final GroupSubResource groupSubResource;

  @Inject
  public GroupV2Resource(GroupCollectionResource groupCollectionResource, GroupSubResource groupSubResource) {
    this.groupCollectionResource = groupCollectionResource;
    this.groupSubResource = groupSubResource;
  }

  @Path("")
  public GroupCollectionResource getGroupCollectionResource() {
    return groupCollectionResource;
  }

  @Path("{id}")
  public GroupSubResource getGroupSubResource() {
    return groupSubResource;
  }
}

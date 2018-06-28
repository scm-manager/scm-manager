package sonia.scm.api.v2.resources;

import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;

import javax.inject.Inject;

public class GroupCollectionToDtoMapper extends BasicCollectionToDtoMapper<Group, GroupDto> {

  private final ResourceLinks resourceLinks;

  @Inject
  public GroupCollectionToDtoMapper(GroupToGroupDtoMapper groupToDtoMapper, ResourceLinks resourceLinks) {
    super("groups", groupToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  @Override
  String createCreateLink() {
    return resourceLinks.groupCollection().create();
  }

  @Override
  String createSelfLink() {
    return resourceLinks.groupCollection().self();
  }

  @Override
  boolean isCreatePermitted() {
    return GroupPermissions.create().isPermitted();
  }
}

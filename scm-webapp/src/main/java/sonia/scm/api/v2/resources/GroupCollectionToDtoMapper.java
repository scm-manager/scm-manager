package sonia.scm.api.v2.resources;

import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;

import javax.inject.Inject;

import static sonia.scm.api.v2.resources.ResourceLinks.groupCollection;

public class GroupCollectionToDtoMapper extends BasicCollectionToDtoMapper<Group, GroupDto> {

  private final UriInfoStore uriInfoStore;

  @Inject
  public GroupCollectionToDtoMapper(GroupToGroupDtoMapper groupToDtoMapper, UriInfoStore uriInfoStore) {
    super("groups", groupToDtoMapper);
    this.uriInfoStore = uriInfoStore;
  }

  @Override
  String createCreateLink() {
    return groupCollection(uriInfoStore.get()).create();
  }

  @Override
  String createSelfLink() {
    return groupCollection(uriInfoStore.get()).self();
  }

  @Override
  boolean isCreatePermitted() {
    return GroupPermissions.create().isPermitted();
  }
}

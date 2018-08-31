package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class GroupCollectionToDtoMapper extends BasicCollectionToDtoMapper<Group, GroupDto, GroupToGroupDtoMapper> {

  private final ResourceLinks resourceLinks;

  @Inject
  public GroupCollectionToDtoMapper(GroupToGroupDtoMapper groupToDtoMapper, ResourceLinks resourceLinks) {
    super("groups", groupToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Group> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(), this.createCreateLink());
  }

  private Optional<String> createCreateLink() {
    return GroupPermissions.create().isPermitted() ? of(resourceLinks.groupCollection().create()): empty();
  }

  private String createSelfLink() {
    return resourceLinks.groupCollection().self();
  }
}

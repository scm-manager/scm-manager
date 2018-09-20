package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class UserCollectionToDtoMapper extends BasicCollectionToDtoMapper<User, UserDto, UserToUserDtoMapper> {

  private final ResourceLinks resourceLinks;

  @Inject
  public UserCollectionToDtoMapper(UserToUserDtoMapper userToDtoMapper, ResourceLinks resourceLinks) {
    super("users", userToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<User> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(), this.createCreateLink());
  }

  Optional<String> createCreateLink() {
    return UserPermissions.create().isPermitted() ? of(resourceLinks.userCollection().create()): empty();
  }

  String createSelfLink() {
    return resourceLinks.userCollection().self();
  }
}

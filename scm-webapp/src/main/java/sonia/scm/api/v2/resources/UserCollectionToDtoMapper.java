package sonia.scm.api.v2.resources;

import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
public class UserCollectionToDtoMapper extends BasicCollectionToDtoMapper<User, UserDto> {

  private final ResourceLinks resourceLinks;

  @Inject
  public UserCollectionToDtoMapper(UserToUserDtoMapper userToDtoMapper, ResourceLinks resourceLinks) {
    super("users", userToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  @Override
  String createCreateLink() {
    return resourceLinks.userCollection().create();
  }

  @Override
  String createSelfLink() {
    return resourceLinks.userCollection().self();
  }

  @Override
  boolean isCreatePermitted() {
    return UserPermissions.create().isPermitted();
  }
}

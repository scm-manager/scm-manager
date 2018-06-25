package sonia.scm.api.v2.resources;

import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;

import static sonia.scm.api.v2.resources.ResourceLinks.userCollection;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
public class UserCollectionToDtoMapper extends BasicCollectionToDtoMapper<User, UserDto> {

  private final UriInfoStore uriInfoStore;

  @Inject
  public UserCollectionToDtoMapper(UserToUserDtoMapper userToDtoMapper, UriInfoStore uriInfoStore) {
    super("users", userToDtoMapper);
    this.uriInfoStore = uriInfoStore;
  }

  @Override
  String createCreateLink() {
    return userCollection(uriInfoStore.get()).create();
  }

  @Override
  String createSelfLink() {
    return userCollection(uriInfoStore.get()).self();
  }

  @Override
  boolean isCreatePermitted() {
    return UserPermissions.create().isPermitted();
  }
}

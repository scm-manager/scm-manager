package sonia.scm.api.v2.resources;

import org.apache.shiro.authc.credential.PasswordService;
import org.mapstruct.*;
import sonia.scm.user.User;

import javax.inject.Inject;

import static sonia.scm.api.rest.resources.UserResource.DUMMY_PASSWORT;

@Mapper
public abstract class UserDtoToUserMapper {

  private PasswordService passwordService;

  UserDtoToUserMapper() {
  }

  @Inject
  public UserDtoToUserMapper(PasswordService passwordService) {
    this.passwordService = passwordService;
  }

  @Mappings({
    @Mapping(source = "password", target = "password", qualifiedByName = "encrypt"),
    @Mapping(target = "creationDate", ignore = true),
    @Mapping(target = "lastModified", ignore = true)
  })
  public abstract User map(UserDto userDto, @Context String originalPassword);

  @Named("encrypt")
  String encrypt(String password, @Context String originalPassword) {

    if (DUMMY_PASSWORT.equals(password)) {
      return originalPassword;
    } else {
      return passwordService.encryptPassword(password);
    }
  }
}

package sonia.scm.api.v2.resources;

import org.apache.shiro.authc.credential.PasswordService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sonia.scm.user.User;

import javax.inject.Inject;

import static sonia.scm.api.rest.resources.UserResource.DUMMY_PASSWORT;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class UserDtoToUserMapper {

  @Inject
  private PasswordService passwordService;

  @Mapping(source = "password", target = "password", qualifiedByName = "encrypt")
  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "lastModified", ignore = true)
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

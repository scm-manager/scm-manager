package sonia.scm.api.rest.resources;

import com.google.inject.Inject;
import org.apache.shiro.authc.credential.PasswordService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sonia.scm.user.User;

import static sonia.scm.api.rest.resources.UserResource.DUMMY_PASSWORT;

@Mapper
public abstract class UserDto2UserMapper {

  @Inject
  private PasswordService passwordService;

  @Mapping(source = "password", target = "password", qualifiedByName = "encrypt")
  public abstract User userDtoToUser(UserDto userDto, @Context String originalPassword);

  @Named("encrypt")
  String encrypt(String password, @Context String originalPassword) {

    if (DUMMY_PASSWORT.equals(password))
    {
      return originalPassword;
    }
    else
    {
      return passwordService.encryptPassword(password);
    }

  }
}

package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import org.apache.shiro.authc.credential.PasswordService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import sonia.scm.user.User;

import static sonia.scm.api.rest.resources.UserResource.DUMMY_PASSWORT;

@Mapper
public abstract class UserDto2UserMapper {

  @Inject
  private PasswordService passwordService;

  @Mappings({
    @Mapping(source = "password", target = "password", qualifiedByName = "encrypt"),
    @Mapping(target = "creationDate", ignore = true),
    @Mapping(target = "lastModified", ignore = true)
  })
  public abstract User map(UserDto userDto, @Context String originalPassword);

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

package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import org.apache.shiro.authc.credential.PasswordService;
import org.mapstruct.*;
import sonia.scm.user.User;

import java.time.Instant;

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

  @Mappings({@Mapping(target = "lastModified"), @Mapping(target = "creationDate")})
  Long mapTime(Instant instant) {
    return instant == null? null: instant.toEpochMilli();
  }
}

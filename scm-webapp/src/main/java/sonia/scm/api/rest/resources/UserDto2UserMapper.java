package sonia.scm.api.rest.resources;

import org.apache.shiro.authc.credential.PasswordService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import sonia.scm.user.User;

import static sonia.scm.api.rest.resources.UserResource.DUMMY_PASSWORT;

@Mapper
public abstract class UserDto2UserMapper {

  public static UserDto2UserMapper INSTANCE = Mappers.getMapper(UserDto2UserMapper.class);

  @Mapping(source = "password", target = "password", qualifiedByName = "encrypt")
  abstract public User userDtoToUser(UserDto userDto, @Context String originalPassword, @Context PasswordService passwordService);

  @Named("encrypt")
  public String encrypt(String password, @Context String originalPassword, @Context PasswordService passwordService) {

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

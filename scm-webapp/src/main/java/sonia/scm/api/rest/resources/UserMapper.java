package sonia.scm.api.rest.resources;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import sonia.scm.user.User;

@Mapper
public interface UserMapper {
      UserMapper INSTANCE = Mappers.getMapper( UserMapper.class );
 
    @Mapping(source = "name", target = "name")
    UserDto userToUserDto(User user);
}

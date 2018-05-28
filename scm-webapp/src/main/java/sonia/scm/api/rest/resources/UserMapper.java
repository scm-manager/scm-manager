package sonia.scm.api.rest.resources;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import sonia.scm.user.User;

import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

@Mapper
public abstract class UserMapper {
  public static UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  abstract public UserDto userToUserDto(User user, @Context UriInfo uriInfo);

  abstract public User userDtoToUser(UserDto user, @Context UriInfo uriInfo);

  @AfterMapping
  public void appendLinks(User source, @MappingTarget UserDto target, @Context UriInfo uriInfo) {
    Map<String, Link> links = new LinkedHashMap<>();
    links.put("self", new Link(uriInfo.getAbsolutePath()));
    target.setLinks(links);
  }
}

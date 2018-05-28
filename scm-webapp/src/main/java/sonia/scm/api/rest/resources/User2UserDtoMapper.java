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
public abstract class User2UserDtoMapper {
  public static User2UserDtoMapper INSTANCE = Mappers.getMapper(User2UserDtoMapper.class);

  abstract public UserDto userToUserDto(User user, @Context UriInfo uriInfo);

  @AfterMapping
  public void removePassword(User source, @MappingTarget UserDto target, @Context UriInfo uriInfo) {
    target.setPassword(UserResource.DUMMY_PASSWORT);
  }

  @AfterMapping
  public void appendLinks(User source, @MappingTarget UserDto target, @Context UriInfo uriInfo) {
    Map<String, Link> links = new LinkedHashMap<>();
    links.put("self", new Link(uriInfo.getAbsolutePath()));
    target.setLinks(links);
  }
}

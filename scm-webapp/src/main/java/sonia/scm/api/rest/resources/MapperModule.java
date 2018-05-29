package sonia.scm.api.rest.resources;

import com.google.inject.AbstractModule;
import org.mapstruct.factory.Mappers;

public class MapperModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(UserDto2UserMapper.class).to(Mappers.getMapper(UserDto2UserMapper.class).getClass());
    bind(User2UserDtoMapper.class).to(Mappers.getMapper(User2UserDtoMapper.class).getClass());
  }
}

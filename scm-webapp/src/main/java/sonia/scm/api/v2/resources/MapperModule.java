package sonia.scm.api.v2.resources;

import com.google.inject.AbstractModule;
import org.mapstruct.factory.Mappers;

public class MapperModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(UserDtoToUserMapper.class).to(Mappers.getMapper(UserDtoToUserMapper.class).getClass());
    bind(UserToUserDtoMapper.class).to(Mappers.getMapper(UserToUserDtoMapper.class).getClass());

    bind(GroupToGroupDtoMapper.class).to(Mappers.getMapper(GroupToGroupDtoMapper.class).getClass());
    bind(GroupDtoToGroupMapper.class).to(Mappers.getMapper(GroupDtoToGroupMapper.class).getClass());
  }
}

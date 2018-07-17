package sonia.scm.api.v2.resources;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletScopes;
import org.mapstruct.factory.Mappers;

public class MapperModule extends AbstractModule {
  @Override
  protected void configure() {

    bind(GitConfigDtoToGitConfigMapper.class).to(Mappers.getMapper(GitConfigDtoToGitConfigMapper.class).getClass());
    bind(GitConfigToGitConfigDtoMapper.class).to(Mappers.getMapper(GitConfigToGitConfigDtoMapper.class).getClass());

    bind(UriInfoStore.class).in(ServletScopes.REQUEST);
  }
}

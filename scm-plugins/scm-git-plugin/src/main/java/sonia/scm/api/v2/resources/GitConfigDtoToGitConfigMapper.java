package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import sonia.scm.repository.GitConfig;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class GitConfigDtoToGitConfigMapper {
  public abstract GitConfig map(GitConfigDto dto);
}

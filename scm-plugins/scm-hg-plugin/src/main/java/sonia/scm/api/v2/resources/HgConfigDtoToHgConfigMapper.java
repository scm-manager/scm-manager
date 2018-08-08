package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import sonia.scm.repository.HgConfig;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class HgConfigDtoToHgConfigMapper {
  public abstract HgConfig map(HgConfigDto dto);
}

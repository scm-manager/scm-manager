package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sonia.scm.installer.HgPackage;

import javax.inject.Inject;

@Mapper
public abstract class HgConfigPackageToDtoMapper extends BaseMapper<HgPackage, HgConfigPackageDto> {
  @Inject
  private UriInfoStore uriInfoStore;

  @Override
  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  @Mapping(target = "hgConfigTemplate.attributes", ignore = true) // Also not for nested DTOs
  public abstract HgConfigPackageDto map(HgPackage modelObject);

  // Don't add links because ConfigPackages don't have their own ressource
}

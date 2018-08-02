package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.installer.HgPackage;

import javax.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class HgConfigPackageToDtoMapper extends BaseMapper<HgPackage, HgConfigPackageDto> {
  @Inject
  private UriInfoStore uriInfoStore;

  @AfterMapping
  void appendLinks(@MappingTarget HgConfigPackageDto target) {
    Links.Builder linksBuilder = linkingTo().self(self());
    target.add(linksBuilder.build());
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), HgConfigPackageResource.class);
    return linkBuilder.method("get").parameters().href();
  }
}

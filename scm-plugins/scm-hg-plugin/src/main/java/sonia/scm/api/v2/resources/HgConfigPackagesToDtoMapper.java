package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import lombok.Getter;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.installer.HgPackage;
import sonia.scm.installer.HgPackages;

import javax.inject.Inject;
import java.util.List;

import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class HgConfigPackagesToDtoMapper  {

  @Inject
  private UriInfoStore uriInfoStore;

  public HgConfigPackagesDto map(HgPackages hgpackages) {
    return map(new HgPackagesNonIterable(hgpackages));
  }

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  /* Favor warning "Unmapped target property: "attributes", to packages[].hgConfigTemplate"
     Over error "Unknown property "packages[].hgConfigTemplate.attributes"
     @Mapping(target = "packages[].hgConfigTemplate.attributes", ignore = true) // Also not for nested DTOs
   */
  protected abstract HgConfigPackagesDto map(HgPackagesNonIterable hgPackagesNonIterable);

  @AfterMapping
  void appendLinks(@MappingTarget HgConfigPackagesDto target) {
    Links.Builder linksBuilder = linkingTo().self(createSelfLink());
    target.add(linksBuilder.build());
  }

  private String createSelfLink() {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), HgConfigResource.class);
    return linkBuilder.method("getPackagesResource").parameters().href();
  }

  /**
   * Unfortunately, HgPackages is iterable, HgConfigPackagesDto does not need to be iterable and MapStruct refuses to
   * map an iterable to a non-iterable. So use this little non-iterable "proxy".
   */
  @Getter
  static class HgPackagesNonIterable {
    private List<HgPackage> packages;

    HgPackagesNonIterable(HgPackages hgPackages) {
      this.packages = hgPackages.getPackages();
    }
  }
}

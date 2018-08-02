package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.HgConfig;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class HgConfigToHgConfigDtoMapper extends BaseMapper<HgConfig, HgConfigDto> {

  @Inject
  private UriInfoStore uriInfoStore;

  @AfterMapping
  void appendLinks(HgConfig config, @MappingTarget HgConfigDto target) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (ConfigurationPermissions.write(config).isPermitted()) {
      linksBuilder.single(link("update", update()));
    }
    target.add(linksBuilder.build());
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), HgConfigResource.class);
    return linkBuilder.method("get").parameters().href();
  }

  private String update() {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), HgConfigResource.class);
    return linkBuilder.method("update").parameters().href();
  }
}

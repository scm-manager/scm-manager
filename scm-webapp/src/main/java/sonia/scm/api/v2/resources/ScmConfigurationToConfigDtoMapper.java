package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class ScmConfigurationToConfigDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract ConfigDto map(ScmConfiguration config);

  @AfterMapping
  void appendLinks(ScmConfiguration config, @MappingTarget ConfigDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.config().self());
    if (ConfigurationPermissions.write(config).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.config().update()));
    }
    target.add(linksBuilder.build());
  }

}

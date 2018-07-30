package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class ScmConfigurationToGlobalConfigDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  public abstract GlobalConfigDto map(ScmConfiguration config);

  @AfterMapping
  void appendLinks(ScmConfiguration config, @MappingTarget GlobalConfigDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.globalConfig().self());
    if (ConfigurationPermissions.write(config).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.globalConfig().update()));
    }
    target.add(linksBuilder.build());
  }

}

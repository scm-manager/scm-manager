package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginState;
import sonia.scm.plugin.PluginWrapper;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class PluginDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  public PluginDto map(PluginWrapper plugin) {
    return map(plugin.getPlugin().getInformation());
  }

  public abstract PluginDto map(PluginInformation plugin);

  @AfterMapping
  protected void appendCategory(@MappingTarget PluginDto dto) {
    if (dto.getCategory() == null) {
      dto.setCategory("Miscellaneous");
    }
  }

  @ObjectFactory
  public PluginDto createDto(PluginInformation pluginInformation) {
    Links.Builder linksBuilder;
    if (pluginInformation.getState() != null && pluginInformation.getState().equals(PluginState.AVAILABLE)) {
      linksBuilder = linkingTo()
        .self(resourceLinks.availablePlugin()
          .self(pluginInformation.getName(), pluginInformation.getVersion()));

      linksBuilder.single(link("install", resourceLinks.availablePlugin().install(pluginInformation.getName(), pluginInformation.getVersion())));
    }
    else {
      linksBuilder = linkingTo()
        .self(resourceLinks.installedPlugin()
          .self(pluginInformation.getName()));
    }

    return new PluginDto(linksBuilder.build());
  }
}

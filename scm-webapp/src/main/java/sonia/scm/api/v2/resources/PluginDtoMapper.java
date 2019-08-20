package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginState;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class PluginDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  public abstract void map(PluginInformation plugin, @MappingTarget PluginDto dto);

  public PluginDto map(Plugin plugin) {
    PluginDto dto = createDto(plugin);
    map(plugin.getDescriptor().getInformation(), dto);
    if (dto.getCategory() == null) {
      dto.setCategory("Miscellaneous");
    }
    return dto;
  }

  private PluginDto createDto(Plugin plugin) {
    Links.Builder linksBuilder;

    PluginInformation pluginInformation = plugin.getDescriptor().getInformation();

    if (plugin.getState() != null && plugin.getState().equals(PluginState.AVAILABLE)) {
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

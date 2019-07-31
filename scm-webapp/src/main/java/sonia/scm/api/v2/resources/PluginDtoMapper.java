package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginState;
import sonia.scm.plugin.PluginWrapper;
import javax.inject.Inject;

import static de.otto.edison.hal.Link.*;
import static de.otto.edison.hal.Links.linkingTo;

public class PluginDtoMapper {

  private final ResourceLinks resourceLinks;

  @Inject
  public PluginDtoMapper(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public PluginDto map(PluginWrapper plugin) {
    return map(plugin.getPlugin().getInformation());
  }

  public PluginDto map(PluginInformation pluginInformation) {
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

    PluginDto pluginDto = new PluginDto(linksBuilder.build());
    pluginDto.setName(pluginInformation.getName());
    pluginDto.setCategory(pluginInformation.getCategory() != null ? pluginInformation.getCategory() : "Miscellaneous");
    pluginDto.setVersion(pluginInformation.getVersion());
    pluginDto.setAuthor(pluginInformation.getAuthor());
    pluginDto.setDescription(pluginInformation.getDescription());

    return pluginDto;
  }
}

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import sonia.scm.plugin.PluginWrapper;
import javax.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

public class PluginDtoMapper {

  private final ResourceLinks resourceLinks;

  @Inject
  public PluginDtoMapper(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public PluginDto map(PluginWrapper plugin) {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.plugin()
        .self(plugin.getId()));

    PluginDto pluginDto = new PluginDto(linksBuilder.build());
    pluginDto.setName(plugin.getPlugin().getInformation().getName());
    pluginDto.setType(plugin.getPlugin().getInformation().getCategory() != null ? plugin.getPlugin().getInformation().getCategory() : "Miscellaneous");
    pluginDto.setVersion(plugin.getPlugin().getInformation().getVersion());
    pluginDto.setAuthor(plugin.getPlugin().getInformation().getAuthor());
    pluginDto.setDescription(plugin.getPlugin().getInformation().getDescription());

    return pluginDto;
  }
}

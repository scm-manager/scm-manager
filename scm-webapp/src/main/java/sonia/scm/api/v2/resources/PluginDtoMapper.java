package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import sonia.scm.plugin.PluginWrapper;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import static de.otto.edison.hal.Links.linkingTo;

public class PluginDtoMapper {

  private final ResourceLinks resourceLinks;
  private final HttpServletRequest request;

  @Inject
  public PluginDtoMapper(ResourceLinks resourceLinks, HttpServletRequest request) {
    this.resourceLinks = resourceLinks;
    this.request = request;
  }

  public PluginDto map(PluginWrapper plugin) {
    PluginDto pluginDto = new PluginDto();
    pluginDto.setName(plugin.getPlugin().getInformation().getName());
    pluginDto.setType(plugin.getPlugin().getInformation().getCategory() != null ? plugin.getPlugin().getInformation().getCategory() : "Miscellaneous");
    pluginDto.setVersion(plugin.getPlugin().getInformation().getVersion());
    pluginDto.setAuthor(plugin.getPlugin().getInformation().getAuthor());
    pluginDto.setDescription(plugin.getPlugin().getInformation().getDescription());

    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.uiPlugin()
        .self(plugin.getId()));

    pluginDto.add(linksBuilder.build());

    return pluginDto;
  }
}

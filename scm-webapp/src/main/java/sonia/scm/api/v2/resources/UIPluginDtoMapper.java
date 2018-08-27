package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import sonia.scm.plugin.PluginWrapper;

import javax.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

public class UIPluginDtoMapper {

  private ResourceLinks resourceLinks;

  @Inject
  public UIPluginDtoMapper(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public UIPluginDto map(PluginWrapper plugin) {
    UIPluginDto dto = new UIPluginDto(
      plugin.getPlugin().getInformation().getName(),
      plugin.getPlugin().getResources().getScriptResources()
    );

    Links.Builder linksBuilder = linkingTo()
        .self(resourceLinks.uiPlugin()
        .self(plugin.getId()));

    dto.add(linksBuilder.build());

    return dto;
  }

}

package sonia.scm.api.v2.resources;

import com.google.common.base.Strings;
import de.otto.edison.hal.Links;
import sonia.scm.plugin.PluginWrapper;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Links.linkingTo;

public class UIPluginDtoMapper {

  private final ResourceLinks resourceLinks;
  private final HttpServletRequest request;

  @Inject
  public UIPluginDtoMapper(ResourceLinks resourceLinks, HttpServletRequest request) {
    this.resourceLinks = resourceLinks;
    this.request = request;
  }

  public UIPluginDto map(PluginWrapper plugin) {
    UIPluginDto dto = new UIPluginDto(
      plugin.getPlugin().getInformation().getName(),
      getScriptResources(plugin)
    );

    Links.Builder linksBuilder = linkingTo()
        .self(resourceLinks.uiPlugin()
        .self(plugin.getId()));

    dto.add(linksBuilder.build());

    return dto;
  }

  private Set<String> getScriptResources(PluginWrapper wrapper) {
    Set<String> scriptResources = wrapper.getPlugin().getResources().getScriptResources();
    if (scriptResources != null) {
      return scriptResources.stream()
        .map(this::addContextPath)
        .collect(Collectors.toSet());
    }
    return Collections.emptySet();
  }

  private String addContextPath(String resource) {
    String ctxPath = request.getContextPath();
    if (Strings.isNullOrEmpty(ctxPath)) {
      return resource;
    }
    return HttpUtil.append(ctxPath, resource);
  }

}

/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import com.google.common.base.Strings;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.util.HttpUtil;

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

  public UIPluginDto map(InstalledPlugin plugin) {
    UIPluginDto dto = new UIPluginDto(
      plugin.getDescriptor().getInformation().getName(),
      getScriptResources(plugin)
    );

    Links.Builder linksBuilder = linkingTo()
        .self(resourceLinks.uiPlugin()
        .self(plugin.getId()));

    dto.add(linksBuilder.build());

    return dto;
  }

  private Set<String> getScriptResources(InstalledPlugin wrapper) {
    Set<String> scriptResources = wrapper.getDescriptor().getResources().getScriptResources();
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
      return "/" + resource;
    }
    return HttpUtil.append(ctxPath, resource);
  }

}

/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

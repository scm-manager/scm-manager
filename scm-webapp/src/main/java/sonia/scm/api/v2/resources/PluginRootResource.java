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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;

@OpenAPIDefinition(tags = {
  @Tag(name = "Plugin Management", description = "Plugin management related endpoints")
})
@Path("v2/plugins")
public class PluginRootResource {

  private final Provider<InstalledPluginResource> installedPluginResourceProvider;
  private final Provider<AvailablePluginResource> availablePluginResourceProvider;
  private final Provider<PendingPluginResource> pendingPluginResourceProvider;
  private final Provider<PluginCenterAuthResource> pluginCenterAuthResourceProvider;

  @Inject
  public PluginRootResource(
    Provider<InstalledPluginResource> installedPluginResourceProvider,
    Provider<AvailablePluginResource> availablePluginResourceProvider,
    Provider<PendingPluginResource> pendingPluginResourceProvider,
    Provider<PluginCenterAuthResource> pluginCenterAuthResourceProvider
  ) {
    this.installedPluginResourceProvider = installedPluginResourceProvider;
    this.availablePluginResourceProvider = availablePluginResourceProvider;
    this.pendingPluginResourceProvider = pendingPluginResourceProvider;
    this.pluginCenterAuthResourceProvider = pluginCenterAuthResourceProvider;
  }

  @Path("/installed")
  public InstalledPluginResource installedPlugins() {
    return installedPluginResourceProvider.get();
  }

  @Path("/available")
  public AvailablePluginResource availablePlugins() { return availablePluginResourceProvider.get(); }

  @Path("/pending")
  public PendingPluginResource pendingPlugins() { return pendingPluginResourceProvider.get(); }

  @Path("/auth")
  public PluginCenterAuthResource authResource() {
    return pluginCenterAuthResourceProvider.get();
  }
}

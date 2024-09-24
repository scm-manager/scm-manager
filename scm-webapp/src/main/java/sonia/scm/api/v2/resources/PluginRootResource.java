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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Path;

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

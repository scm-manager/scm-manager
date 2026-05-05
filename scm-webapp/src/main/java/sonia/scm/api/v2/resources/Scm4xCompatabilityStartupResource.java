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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthenticatedException;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.initialization.InitializationStepResource;
import sonia.scm.lifecycle.PrivilegedStartupAction;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.lifecycle.Scm4xCompatibilityStartupAction;
import sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Result;
import sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Status;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginManager;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.web.security.AdministrationContext;

import java.util.List;

import static de.otto.edison.hal.Link.link;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@AllowAnonymousAccess
@Extension
@Slf4j
public class Scm4xCompatabilityStartupResource implements InitializationStepResource {

  private final Scm4xCompatibilityStartupAction startupAction;
  private final ScmPathInfoStore scmPathInfoStore;
  private final ScmConfigurationStore configurationStore;
  private final PluginManager pluginManager;
  private final Restarter restarter;
  private final AdministrationContext adminContext;

  @Inject
  public Scm4xCompatabilityStartupResource(Scm4xCompatibilityStartupAction startupAction,
                                           ScmPathInfoStore scmPathInfoStore,
                                           ScmConfigurationStore configurationStore,
                                           PluginManager pluginManager,
                                           Restarter restarter,
                                           AdministrationContext adminContext) {
    this.startupAction = startupAction;
    this.scmPathInfoStore = scmPathInfoStore;
    this.configurationStore = configurationStore;
    this.pluginManager = pluginManager;
    this.restarter = restarter;
    this.adminContext = adminContext;
  }

  @POST
  @Path("uninstall")
  @Consumes(MediaType.APPLICATION_JSON)
  public void uninstallConflictingPlugins(@Valid TokenDto tokenDto) {
    checkToken(tokenDto);
    Result result = startupAction.getCompatibilityResult();
    doThrow()
      .violation("Uninstalling conflicting plugins is only allowed, if conflicting plugins exist")
      .when(!isUninstallAllowed(result));

    adminContext.runAsAdmin((PrivilegedStartupAction) () -> {
      for (InstalledPlugin conflictingPlugin : startupAction.getCompatibilityResult().conflictingPlugins()) {
        log.trace("Uninstalling plugin {}", conflictingPlugin);

        if (!conflictingPlugin.isMarkedForUninstall()) {
          pluginManager.uninstall(conflictingPlugin.getDescriptor().getInformation().getName(), false);
        }
      }

      if (!restarter.isSupported()) {
        throw new RestartNotSupportedException();
      }

      restarter.restart(Scm4xCompatabilityStartupResource.class, "Finish removal of SCM 4.x incompatible plugins");
    });
  }

  @POST
  @Path("plugin-center")
  @Consumes(MediaType.APPLICATION_JSON)
  public void updatePluginUrl(@Valid PluginCenterUrlDto pluginCenterUrlDto) {
    checkToken(pluginCenterUrlDto);
    Result result = startupAction.getCompatibilityResult();
    doThrow()
      .violation("Setting the plugin center url is only allowed, if incompatible plugins exist and if the plugin center is deactivated")
      .when(!isUpdatingPluginCenterUrlAllowed(result));

    adminContext.runAsAdmin((PrivilegedStartupAction) () -> {
      ScmConfiguration configuration = configurationStore.get();
      configuration.setPluginUrl(pluginCenterUrlDto.getPluginCenterUrl());
      configurationStore.store(configuration);

      if (!restarter.isSupported()) {
        throw new RestartNotSupportedException();
      }

      restarter.restart(Scm4xCompatabilityStartupResource.class, "Retrigger SCM 4.x compatibility check after setting plugin center url");
    });
  }

  private void checkToken(TokenDto tokenDto) {
    if (!tokenDto.getToken().equals(startupAction.getToken())) {
      throw new UnauthenticatedException("Wrong token");
    }
  }

  @Override
  public String name() {
    return startupAction.name();
  }

  @Override
  public void setupIndex(Links.Builder builder, Embedded.Builder embeddedBuilder) {
    Result result = startupAction.getCompatibilityResult();

    if (result.status() == Status.NO_ACTION_NEEDED) {
      return;
    }

    embeddedBuilder.with(name(), map(result));
  }

  private boolean isUninstallAllowed(Result result) {
    return result.status() == Status.UPDATE_NOT_POSSIBLE;
  }

  private boolean isUpdatingPluginCenterUrlAllowed(Result result) {
    return result.status() == Status.PLUGIN_CENTER_DEACTIVATED || result.status() == Status.UPDATE_NOT_POSSIBLE;
  }

  private ResultDto map(Result result) {
    ResultDto dto = new ResultDto(
      result.status(),
      result.conflictingPlugins().stream().map(this::map).toList(),
      configurationStore.get().getPluginUrl()
    );

    Links.Builder links = new Links.Builder();
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), InitializationResource.class, Scm4xCompatabilityStartupResource.class);

    if (isUninstallAllowed(result)) {
      links.single(link(
        "uninstall",
        linkBuilder.method("step").parameters(name()).method("uninstallConflictingPlugins").parameters().href()
      ));
    }

    if (isUpdatingPluginCenterUrlAllowed(result)) {
      links.single(link(
        "update-plugin-center",
        linkBuilder.method("step").parameters(name()).method("updatePluginUrl").parameters().href()
      ));
    }

    dto.add(links.build());

    return dto;
  }

  private SimplePluginDto map(Plugin plugin) {
    return new SimplePluginDto(
      plugin.getDescriptor().getInformation().getDisplayName(),
      plugin.getDescriptor().getInformation().getVersion()
    );
  }

  @EqualsAndHashCode(callSuper = true)
  @Data
  static class ResultDto extends HalRepresentation {
    private final Status status;
    private final List<SimplePluginDto> conflictingPlugins;
    private final String currentPluginCenterUrl;
    private final String defaultPluginCenterUrl = ScmConfiguration.DEFAULT_PLUGIN_URL;

    @Override
    protected HalRepresentation add(Links links) {
      return super.add(links);
    }
  }

  record SimplePluginDto(String name, String version) {
  }

  @Data
  public static class TokenDto {

    @NotBlank
    private String token;
  }

  @EqualsAndHashCode(callSuper = true)
  @Data
  public static class PluginCenterUrlDto extends TokenDto {

    @NotBlank
    //Plugin Center url needs to start with "http://" or "https://"
    @Pattern(regexp = "^https?://.*")
    private String pluginCenterUrl;
  }
}

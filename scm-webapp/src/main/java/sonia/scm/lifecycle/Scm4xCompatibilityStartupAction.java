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

package sonia.scm.lifecycle;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginManager;
import sonia.scm.version.Version;
import sonia.scm.web.security.AdministrationContext;

import java.util.List;

@Extension
@Singleton
@Slf4j
public class Scm4xCompatibilityStartupAction implements InitializationStep {

  private final PluginManager pluginManager;
  private final ScmConfiguration configuration;
  private final AdministrationContext adminContext;
  private final Restarter restarter;
  private final RandomPasswordGenerator randomPasswordGenerator;
  private Result result;
  private String token;

  @Inject
  public Scm4xCompatibilityStartupAction(PluginManager pluginManager,
                                         ScmConfiguration configuration,
                                         AdministrationContext adminContext,
                                         Restarter restarter,
                                         @SuppressWarnings("ClassEscapesDefinedScope") RandomPasswordGenerator randomPasswordGenerator) {
    this.pluginManager = pluginManager;
    this.configuration = configuration;
    this.adminContext = adminContext;
    this.restarter = restarter;
    this.randomPasswordGenerator = randomPasswordGenerator;
  }

  @Override
  public String name() {
    return "scm4xCompatibility";
  }

  @Override
  public int sequence() {
    return 200;
  }

  @Override
  public boolean done() {
    if (getCompatibilityResult().isDone()) {
      return true;
    } else {
      logResult();
      return false;
    }
  }

  public Result getCompatibilityResult() {
    if (result == null) {
      checkCompatability();
    }

    return result;
  }

  public String getToken() {
    if (token == null) {
      generateTokenOnce();
    }

    return token;
  }

  private void generateTokenOnce() {
    if (token == null) {
      token = randomPasswordGenerator.createRandomPassword();
    }
  }

  private void logResult() {
    log.warn("====================================================");
    log.warn("==                                                ==");
    log.warn("== The update to SCM-Manager 4 needs intervention ==");
    log.warn("==                                                ==");

    switch (result.status()) {
      case PLUGIN_CENTER_DEACTIVATED -> {
        log.warn("== Some of the plugins are incompatible with v4.  ==");
        log.warn("== The plugin center is deactivated or invalid so ==");
        log.warn("== they cannot be updated automatically.          ==");
        log.warn("== Visit the web frontend to delete these plugins ==");
        log.warn("== or to enter a valid plugin center URL.         ==");
        logToken();
      }
      case UPDATE_NOT_POSSIBLE -> {
        log.warn("== Some of the plugins are incompatible with v4.  ==");
        log.warn("== In the current plugin center, there are no     ==");
        log.warn("== versions compatible with v4 for these plugins. ==");
        log.warn("== Visit the web frontend to delete these plugins ==");
        log.warn("== or to enter a valid plugin center URL.         ==");
        logToken();
      }
      case UPDATED -> {
        log.warn("== Some plugins had to be updated to be           ==");
        log.warn("== compatible with v4.                            ==");
        log.warn("== To finish this update, the server has to be    ==");
        log.warn("== restarted once.                                ==");
      }
    }
    log.warn("==                                                ==");
    log.warn("====================================================");
  }

  private void logToken() {
    log.warn("==                                                ==");
    log.warn("==    Token for initialization authentication     ==");
    log.warn("==                                                ==");
    log.warn("==              {}              ==", getToken());
  }

  @Inject
  void checkCompatability() {
    adminContext.runAsAdmin((PrivilegedStartupAction) () -> {
      List<InstalledPlugin> incompatiblePlugins = pluginManager.getInstalled()
        .stream()
        .filter(plugin -> !isPlugin4xCompatible(plugin))
        .toList();

      if (incompatiblePlugins.isEmpty()) {
        result = new Result(Status.NO_ACTION_NEEDED, List.of());
        return;
      }
      log.info("Found {} incompatible plugins: {}", incompatiblePlugins.size(), incompatiblePlugins);

      if (isPluginCenterUrlInvalid()) {
        log.error("Invalid plugin center url configuration: {}", configuration.getPluginUrl());
        result = new Result(Status.PLUGIN_CENTER_DEACTIVATED, incompatiblePlugins);
        return;
      }

      List<AvailablePlugin> availablePlugins = pluginManager.getAvailable();
      List<InstalledPlugin> withoutCompatibleUpdate = incompatiblePlugins
        .stream()
        .filter(plugin -> withoutCompatibleUpdate(plugin, availablePlugins))
        .toList();

      if (withoutCompatibleUpdate.isEmpty()) {
        log.info("Update all incompatible plugins");
        pluginManager.updateAll();

        if (restarter.isSupported()) {
          restarter.restart(Scm4xCompatibilityStartupAction.class, "Finish SCM 4 plugin update");
        }

        result = new Result(Status.UPDATED, List.of());
        return;
      }

      log.error("Found {} not updatable plugins: {}", withoutCompatibleUpdate.size(), withoutCompatibleUpdate);
      result = new Result(Status.UPDATE_NOT_POSSIBLE, withoutCompatibleUpdate);
    });
  }

  private boolean isPluginCenterUrlInvalid() {
    String pluginCenterUrl = configuration.getPluginUrl();
    return Strings.isNullOrEmpty(pluginCenterUrl) ||
      !(pluginCenterUrl.startsWith("http://") || pluginCenterUrl.startsWith("https://"));
  }

  private boolean isPlugin4xCompatible(Plugin plugin) {
    return plugin.getDescriptor().getInformation().isScm4Compatible() ||
      Version.parse(plugin.getDescriptor().getCondition().getMinVersion()).getMajor() > 3;
  }

  private boolean withoutCompatibleUpdate(InstalledPlugin plugin, List<AvailablePlugin> availablePlugins) {
    return availablePlugins
      .stream()
      .noneMatch(availablePlugin -> isSamePlugin(availablePlugin, plugin));
  }

  private boolean isSamePlugin(AvailablePlugin availablePlugin, InstalledPlugin plugin) {
   return availablePlugin.getDescriptor().getInformation().getName().equals(
     plugin.getDescriptor().getInformation().getName()
   );
  }

  public record Result(Status status, List<InstalledPlugin> conflictingPlugins) {

    boolean isDone() {
      return status == Status.NO_ACTION_NEEDED;
    }
  }

  public enum Status {
    NO_ACTION_NEEDED,
    UPDATED,
    UPDATE_NOT_POSSIBLE,
    PLUGIN_CENTER_DEACTIVATED
  }
}

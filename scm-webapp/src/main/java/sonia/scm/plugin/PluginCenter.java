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

package sonia.scm.plugin;

import com.github.legman.Subscribe;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.config.ScmConfigurationChangedEvent;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.SystemUtil;

import java.util.Set;

@Singleton
public class PluginCenter {

  private static final String PLUGIN_CENTER_RESULT_CACHE_NAME = "sonia.cache.plugin-center";

  private static final Logger LOG = LoggerFactory.getLogger(PluginCenter.class);

  private final SCMContextProvider context;
  private final ScmConfiguration configuration;
  private final PluginCenterLoader loader;
  private final Cache<String, PluginCenterResult> pluginCenterResultCache;

  @Inject
  public PluginCenter(SCMContextProvider context, CacheManager cacheManager, ScmConfiguration configuration, PluginCenterLoader loader) {
    this.context = context;
    this.configuration = configuration;
    this.loader = loader;
    this.pluginCenterResultCache = cacheManager.getCache(PLUGIN_CENTER_RESULT_CACHE_NAME);
  }

  @Subscribe
  public void handle(PluginCenterAuthenticationEvent event) {
    LOG.debug("clear plugin center cache, because of {}", event);
    pluginCenterResultCache.clear();
  }

  @Subscribe
  public void handle(ScmConfigurationChangedEvent event) {
    LOG.debug("clear plugin center cache, because of {}", event);
    pluginCenterResultCache.clear();
  }

  synchronized Set<AvailablePlugin> getAvailablePlugins() {
    String url = buildPluginUrl(configuration.getPluginUrl());
    return getPluginCenterResult(url).getPlugins();
  }

  synchronized Set<PluginSet> getAvailablePluginSets() {
    String url = buildPluginUrl(configuration.getPluginUrl());
    return getPluginCenterResult(url).getPluginSets();
  }

  synchronized PluginCenterResult getPluginResult() {
    String url = buildPluginUrl(configuration.getPluginUrl());
    return getPluginCenterResult(url);
  }

  private PluginCenterResult getPluginCenterResult(String url) {
    PluginCenterResult pluginCenterResult = pluginCenterResultCache.get(url);
    if (pluginCenterResult == null) {
      LOG.debug("no cached plugin center result found, start fetching");
      pluginCenterResult = fetchPluginCenter(url);
    } else {
      LOG.debug("return plugin center result from cache");
    }
    return pluginCenterResult;
  }

  @CanIgnoreReturnValue
  private PluginCenterResult fetchPluginCenter(String url) {
    PluginCenterResult pluginCenterResult = loader.load(url);
    pluginCenterResultCache.put(url, pluginCenterResult);
    return pluginCenterResult;
  }

  synchronized void refresh() {
    LOG.debug("refresh plugin center cache");
    String url = buildPluginUrl(configuration.getPluginUrl());
    fetchPluginCenter(url);
  }

  private String buildPluginUrl(String url) {
    String os = HttpUtil.encode(SystemUtil.getOS());
    String arch = SystemUtil.getArch();
    String javaVersion = SystemUtil.getJre();
    return url.replace("{version}", context.getVersion())
      .replace("{os}", os)
      .replace("{arch}", arch)
      .replace("{jre}", javaVersion);
  }

}

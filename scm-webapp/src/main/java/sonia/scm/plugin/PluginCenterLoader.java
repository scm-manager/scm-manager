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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;

import static sonia.scm.plugin.Tracing.SPAN_KIND;

class PluginCenterLoader {

  private static final Logger LOG = LoggerFactory.getLogger(PluginCenterLoader.class);

  private final AdvancedHttpClient client;
  private final PluginCenterDtoMapper mapper;
  private final ScmEventBus eventBus;

  @Inject
  public PluginCenterLoader(AdvancedHttpClient client, ScmEventBus eventBus) {
    this(client, PluginCenterDtoMapper.INSTANCE, eventBus);
  }

  @VisibleForTesting
  PluginCenterLoader(
    AdvancedHttpClient client,
    PluginCenterDtoMapper mapper,
    ScmEventBus eventBus
  ) {
    this.client = client;
    this.mapper = mapper;
    this.eventBus = eventBus;
  }

  PluginCenterResult load(String url) {
    try {
      if (Strings.isNullOrEmpty(url)) {
        LOG.info("plugin center is deactivated, returning empty list");
        return new PluginCenterResult(PluginCenterStatus.DEACTIVATED);
      }
      LOG.info("fetch plugins from {}", url);
      PluginCenterDto pluginCenterDto = client.get(url).spanKind(SPAN_KIND).request()
        .contentFromJson(PluginCenterDto.class);
      return mapper.map(pluginCenterDto);
    } catch (Exception ex) {
      LOG.error("failed to load plugins from plugin center, returning empty list", ex);
      eventBus.post(new PluginCenterErrorEvent());
      return new PluginCenterResult(PluginCenterStatus.ERROR);
    }
  }
}

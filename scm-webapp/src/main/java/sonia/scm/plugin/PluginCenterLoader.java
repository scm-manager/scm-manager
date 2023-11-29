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
  private final PluginCenterAuthenticator authenticator;
  private final PluginCenterDtoMapper mapper;
  private final ScmEventBus eventBus;

  @Inject
  public PluginCenterLoader(AdvancedHttpClient client, ScmEventBus eventBus, PluginCenterAuthenticator authenticator) {
    this(client, authenticator, PluginCenterDtoMapper.INSTANCE, eventBus);
  }

  @VisibleForTesting
  PluginCenterLoader(
    AdvancedHttpClient client,
    PluginCenterAuthenticator authenticator,
    PluginCenterDtoMapper mapper,
    ScmEventBus eventBus
  ) {
    this.client = client;
    this.authenticator = authenticator;
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
      AdvancedHttpRequest request = client.get(url).spanKind(SPAN_KIND);
      if (authenticator.isAuthenticated()) {
        authenticator.fetchAccessToken().ifPresent(request::bearerAuth);
      }
      PluginCenterDto pluginCenterDto = request.request().contentFromJson(PluginCenterDto.class);
      return mapper.map(pluginCenterDto);
    } catch (Exception ex) {
      LOG.error("failed to load plugins from plugin center, returning empty list", ex);
      eventBus.post(new PluginCenterErrorEvent());
      return new PluginCenterResult(PluginCenterStatus.ERROR);
    }
  }
}

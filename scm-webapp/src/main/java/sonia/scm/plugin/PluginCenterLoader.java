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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.ahc.AdvancedHttpClient;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

class PluginCenterLoader {

  private static final Logger LOG = LoggerFactory.getLogger(PluginCenterLoader.class);

  private final AdvancedHttpClient client;
  private final PluginCenterDtoMapper mapper;

  @Inject
  public PluginCenterLoader(AdvancedHttpClient client) {
    this(client, PluginCenterDtoMapper.INSTANCE);
  }

  @VisibleForTesting
  PluginCenterLoader(AdvancedHttpClient client, PluginCenterDtoMapper mapper) {
    this.client = client;
    this.mapper = mapper;
  }

  Set<AvailablePlugin> load(String url) {
    try {
      LOG.info("fetch plugins from {}", url);
      PluginCenterDto pluginCenterDto = client.get(url).request().contentFromJson(PluginCenterDto.class);
      return mapper.map(pluginCenterDto);
    } catch (Exception ex) {
      LOG.error("failed to load plugins from plugin center, returning empty list", ex);
      return Collections.emptySet();
    }
  }

}

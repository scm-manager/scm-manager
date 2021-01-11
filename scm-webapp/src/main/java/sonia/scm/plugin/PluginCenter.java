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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.SystemUtil;

import javax.inject.Inject;
import java.util.Set;

public class PluginCenter {

  private static final String CACHE_NAME = "sonia.cache.plugins";

  private static final Logger LOG = LoggerFactory.getLogger(PluginCenter.class);

  private final SCMContextProvider context;
  private final ScmConfiguration configuration;
  private final PluginCenterLoader loader;
  private final Cache<String, Set<AvailablePlugin>> cache;

  @Inject
  public PluginCenter(SCMContextProvider context, CacheManager cacheManager, ScmConfiguration configuration, PluginCenterLoader loader) {
    this.context = context;
    this.configuration = configuration;
    this.loader = loader;
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  synchronized Set<AvailablePlugin> getAvailable() {
    String url = buildPluginUrl(configuration.getPluginUrl());
    Set<AvailablePlugin> plugins = cache.get(url);
    if (plugins == null) {
      LOG.debug("no cached available plugins found, start fetching");
      plugins = loader.load(url);
      cache.put(url, plugins);
    } else {
      LOG.debug("return available plugins from cache");
    }
    return plugins;
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

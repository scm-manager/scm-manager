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
    return url.replace("{version}", context.getVersion())
      .replace("{os}", os)
      .replace("{arch}", arch);
  }

}

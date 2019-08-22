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
    } catch (IOException ex) {
      LOG.error("failed to load plugins from plugin center, returning empty list");
      return Collections.emptySet();
    }
  }

}

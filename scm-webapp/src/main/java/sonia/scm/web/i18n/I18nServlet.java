package sonia.scm.web.i18n;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.legman.Subscribe;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContext;
import sonia.scm.Stage;
import sonia.scm.boot.RestartEvent;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.filter.WebElement;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.UberClassLoader;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * Collect the plugin translations.
 */
@Singleton
@WebElement(value = I18nServlet.PATTERN, regex = true)
@Slf4j
public class I18nServlet extends HttpServlet {

  public static final String PATH = "/locales";
  public static final String PLUGINS_JSON = "plugins.json";
  public static final String PATTERN = PATH + "/[a-z\\-A-Z]*/" + PLUGINS_JSON;
  public static final String CACHE_NAME = "sonia.cache.plugins.translations";

  private final UberClassLoader uberClassLoader;
  private final Cache<String, Map> cache;
  private static ObjectMapper objectMapper = new ObjectMapper();


  @Inject
  public I18nServlet(PluginLoader pluginLoader, CacheManager cacheManager) {
    this.uberClassLoader = (UberClassLoader) pluginLoader.getUberClassLoader();
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  @Subscribe(async = false)
  public void handleRestartEvent(RestartEvent event) {
    log.info("Clear cache on restart event with reason {}", event.getReason());
    cache.clear();
  }

  private Map getCollectedJson(String path,
                               Function<String, Optional<Map>> jsonFileProvider,
                               BiConsumer<String, Map> createdJsonFileConsumer) {
    return Optional.ofNullable(jsonFileProvider.apply(path)
      .orElseGet(() -> {
          Optional<Map> createdFile = collectJsonFile(path);
          createdFile.ifPresent(map -> createdJsonFileConsumer.accept(path, map));
          return createdFile.orElse(null);
        }
      )).orElseThrow(NotFoundException::new);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse response) {
    try {
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      String path = req.getServletPath();
      Function<String, Optional<Map>> jsonFileProvider = usedPath -> Optional.empty();
      BiConsumer<String, Map> createdJsonFileConsumer = (usedPath, foundJsonMap) -> log.info("A json File is created from the path {}", usedPath);
      if (isProductionStage()) {
        log.info("In Production Stage get the plugin translations from the cache");
        jsonFileProvider = usedPath -> Optional.ofNullable(
          cache.get(usedPath));
        createdJsonFileConsumer = createdJsonFileConsumer
          .andThen((usedPath, map) -> log.info("Put the created json File in the cache with the key {}", usedPath))
          .andThen(cache::put);
      }
      out.write(objectMapper.writeValueAsString(getCollectedJson(path, jsonFileProvider, createdJsonFileConsumer)));
    } catch (IOException e) {
      log.error("Error on getting the translation of the plugins", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } catch (NotFoundException e) {
      log.error("Plugin translations are not found", e);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  protected boolean isProductionStage() {
    return SCMContext.getContext().getStage() == Stage.PRODUCTION;
  }

  /**
   * Return a collected Json File as map from the given path from all plugins in the class path
   *
   * @param path the searched resource path
   * @return a collected Json File as map from the given path from all plugins in the class path
   */
  protected Optional<Map> collectJsonFile(String path) {
    log.info("Collect plugin translations from path {} for every plugin", path);
    Map result = null;
    try {
      Enumeration<URL> resources = uberClassLoader.getResources(path.replaceFirst("/", ""));
      if (resources.hasMoreElements()) {
        result = new HashMap();
        while (resources.hasMoreElements()) {
          URL url = resources.nextElement();
          result.putAll(objectMapper.readValue(Files.readAllBytes(Paths.get(url.getPath())), Map.class));
        }
      }
    } catch (IOException e) {
      log.error("Error on loading sources from {}", path, e);
      return Optional.empty();
    }
    return Optional.ofNullable(result);
  }
}

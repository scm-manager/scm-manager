package sonia.scm.api.v2.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Set;

/**
 * Registers every {@link LinkEnricher} which is annotated with an {@link Enrich} annotation.
 */
@Extension
public class LinkEnricherAutoRegistration implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(LinkEnricherAutoRegistration.class);

  private final LinkEnricherRegistry registry;
  private final Set<LinkEnricher> enrichers;

  @Inject
  public LinkEnricherAutoRegistration(LinkEnricherRegistry registry, Set<LinkEnricher> enrichers) {
    this.registry = registry;
    this.enrichers = enrichers;
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    for (LinkEnricher enricher : enrichers) {
      Enrich annotation = enricher.getClass().getAnnotation(Enrich.class);
      if (annotation != null) {
        registry.register(annotation.value(), enricher);
      } else {
        LOG.warn("found LinkEnricher extension {} without Enrich annotation", enricher.getClass());
      }
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // nothing todo
  }
}

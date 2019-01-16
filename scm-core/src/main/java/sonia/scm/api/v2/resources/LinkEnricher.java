package sonia.scm.api.v2.resources;

import sonia.scm.plugin.ExtensionPoint;

/**
 * A {@link LinkEnricher} can be used to append hateoas links to a specific json response.
 * To register an enricher use the {@link Enrich} annotation or the {@link LinkEnricherRegistry} which is available
 * via injection.
 *
 * <b>Warning:</b> enrichers are always registered as singletons.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@ExtensionPoint
@FunctionalInterface
public interface LinkEnricher {

  /**
   * Enriches the response with hateoas links.
   *
   * @param context contains the source for the json mapping and related objects
   * @param appender can be used to append links to the json response
   */
  void enrich(LinkEnricherContext context, LinkAppender appender);
}

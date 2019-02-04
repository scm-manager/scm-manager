package sonia.scm.api.v2.resources;

import sonia.scm.plugin.ExtensionPoint;

/**
 * A {@link HalEnricher} can be used to append hal specific attributes, such as links, to the json response.
 * To register an enricher use the {@link Enrich} annotation or the {@link HalEnricherRegistry} which is available
 * via injection.
 *
 * <b>Warning:</b> enrichers are always registered as singletons.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@ExtensionPoint
@FunctionalInterface
public interface HalEnricher {

  /**
   * Enriches the response with hal specific attributes.
   *
   * @param context contains the source for the json mapping and related objects
   * @param appender can be used to append links or embedded objects to the json response
   */
  void enrich(HalEnricherContext context, HalAppender appender);
}

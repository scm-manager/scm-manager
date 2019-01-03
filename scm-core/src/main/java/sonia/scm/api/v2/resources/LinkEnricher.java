package sonia.scm.api.v2.resources;

/**
 * A {@link LinkEnricher} can be used to append hateoas links to a specific json response.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
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

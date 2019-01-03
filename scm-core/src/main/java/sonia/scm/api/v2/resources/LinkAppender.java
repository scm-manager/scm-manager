package sonia.scm.api.v2.resources;

/**
 * The {@link LinkAppender} can be used within an {@link LinkEnricher} to append hateoas links to a json response.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public interface LinkAppender {

  /**
   * Appends one link to the json response.
   *
   * @param rel name of relation
   * @param href link uri
   */
  void appendOne(String rel, String href);
}

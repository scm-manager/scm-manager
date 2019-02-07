package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;

/**
 * The {@link HalAppender} can be used within an {@link HalEnricher} to append hateoas links to a json response.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public interface HalAppender {

  /**
   * Appends one link to the json response.
   *
   * @param rel name of relation
   * @param href link uri
   */
  void appendLink(String rel, String href);

  /**
   * Returns a builder which is able to append an array of links to the resource.
   *
   * @param rel name of link relation
   * @return multi link builder
   */
  LinkArrayBuilder linkArrayBuilder(String rel);

  /**
   * Appends one embedded to the json response.
   *
   * @param rel name of relation
   * @param embeddedItem embedded object
   */
  void appendEmbedded(String rel, HalRepresentation embeddedItem);

  /**
   * Builder for link arrays.
   */
  interface LinkArrayBuilder {

    /**
     * Append an link to the array.
     *
     * @param name name of link
     * @param href link target
     * @return {@code this}
     */
    LinkArrayBuilder append(String name, String href);

    /**
     * Builds the array and appends the it to the json response.
     */
    void build();
  }
}

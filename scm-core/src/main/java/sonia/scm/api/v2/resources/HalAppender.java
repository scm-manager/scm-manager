package sonia.scm.api.v2.resources;

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
  void appendOne(String rel, String href);

  /**
   * Returns a builder which is able to append an array of links to the resource.
   *
   * @param rel name of link relation
   * @return multi link builder
   */
  LinkArrayBuilder arrayBuilder(String rel);


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

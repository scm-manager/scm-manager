/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;

import java.util.List;

/**
 * The {@link HalAppender} can be used within an {@link HalEnricher} to append hateoas links to a json response.
 *
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
   * Appends one embedded object to the json response.
   *
   * @param rel name of relation
   * @param embeddedItem embedded object
   */
  void appendEmbedded(String rel, HalRepresentation embeddedItem);

  /**
   * Appends a list of embedded objects to the json response.
   *
   * @param rel name of relation
   * @param embeddedItems embedded objects
   */
  void appendEmbedded(String rel, List<HalRepresentation> embeddedItems);

  /**
   * Builder for link arrays.
   */
  interface LinkArrayBuilder {

    /**
     * Append a link to the array.
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

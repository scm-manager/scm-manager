/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

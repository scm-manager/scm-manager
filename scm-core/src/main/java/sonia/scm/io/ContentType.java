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

package sonia.scm.io;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Detected type of content.
 *
 * @since 2.23.0
 */
public interface ContentType {

  /**
   * Returns the primary part of the content type (e.g.: text of text/plain).
   */
  String getPrimary();

  /**
   * Returns the secondary part of the content type (e.g.: plain of text/plain).
   */
  String getSecondary();

  /**
   * Returns the raw presentation of the content type (e.g.: text/plain).
   */
  String getRaw();

  /**
   * Returns {@code true} if the content type is text based.
   */
  boolean isText();

  /**
   * Returns an optional with the programming language
   * or empty if the content is not programming language.
   */
  Optional<String> getLanguage();

  /**
   * Returns a map of syntax modes such as codemirror, ace or prism.
   * @since 2.28.0
   */
  default Map<String, String> getSyntaxModes() {
    return Collections.emptyMap();
  }
}

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

package sonia.scm.update;

/**
 * Use this to access old properties from an instance of SCM-Manager v1.
 */
public interface V1PropertyDAO {
  /**
   * Creates an instance of a property reader to process old properties.
   * @param reader The reader for the origin of the properties (for example
   *   {@link V1PropertyReader#REPOSITORY_PROPERTY_READER} for properties of repositories).
   * @return The reader instance.
   */
  V1PropertyReader.Instance getProperties(V1PropertyReader reader);
}

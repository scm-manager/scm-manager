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

package sonia.scm;

import java.util.Map;

/**
 * Base interface of all objects which have properties.
 *
 * @since 1.6
 */
public interface PropertiesAware
{

  public void removeProperty(String key);


  /**
   * Returns all properties.
   */
  public Map<String, String> getProperties();

  /**
   * Returns the property value for the given key
   * or null if the key does not exist.
   *
   * @param key - the key of the property
   */
  public String getProperty(String key);


  /**
   * Sets all properties and overwrites existing ones.
   *
   * @param properties to set
   */
  public void setProperties(Map<String, String> properties);

  /**
   * Sets a single property.
   *
   * @param key - The key of the property
   * @param value - The value of the property
   */
  public void setProperty(String key, String value);
}

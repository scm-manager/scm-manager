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
    
package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import java.util.Map;

/**
 * Base interface of all objects which have properties.
 *
 * @since 1.6
 * @author Sebastian Sdorra
 */
public interface PropertiesAware
{

  /**
   * Removes a existing property.
   *
   * @param key - the key of the property
   */
  public void removeProperty(String key);

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns all properties.
   *
   * @return all properties
   */
  public Map<String, String> getProperties();

  /**
   * Returns the property value for the given key
   * or null if the key does not exists.
   *
   * @param key - the key of the property
   * @return the value of the property
   */
  public String getProperty(String key);

  //~--- set methods ----------------------------------------------------------

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

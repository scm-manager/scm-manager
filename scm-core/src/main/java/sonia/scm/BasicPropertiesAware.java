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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sonia.scm.xml.XmlMapStringAdapter;

import java.io.Serializable;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

/**
 * Default implementation of {@link PropertiesAware} interface.
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicPropertiesAware implements PropertiesAware, Serializable
{

  /** Field description */
  private static final long serialVersionUID = -536608122577385802L;

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final BasicPropertiesAware other = (BasicPropertiesAware) obj;

    return Objects.equal(properties, other.properties);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(properties);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void removeProperty(String key)
  {
    getProperties().remove(key);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public Map<String, String> getProperties()
  {
    if (properties == null)
    {
      properties = Maps.newHashMap();
    }

    return properties;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public String getProperty(String key)
  {
    return getProperties().get(key);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void setProperties(Map<String, String> properties)
  {
    this.properties = properties;
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void setProperty(String key, String value)
  {
    getProperties().put(key, value);
  }

  //~--- fields ---------------------------------------------------------------

  /** map to hold the properties */
  @XmlJavaTypeAdapter(XmlMapStringAdapter.class)
  protected Map<String, String> properties;
}

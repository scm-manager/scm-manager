/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.xml.XmlMapStringAdapter;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Default implementation of {@link PropertiesAware} interface.
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicPropertiesAware implements PropertiesAware
{

  /**
   * Returns true if this object is the same as the obj argument.
   *
   *
   * @param obj - the reference object with which to compare
   *
   * @return true if this object is the same as the obj argument
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

    if ((this.properties != other.properties)
        && ((this.properties == null)
            ||!this.properties.equals(other.properties)))
    {
      return false;
    }

    return true;
  }

  /**
   * Returns a hash code value for this object.
   *
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode()
  {
    int hash = 7;

    hash = 41 * hash + ((this.properties != null)
                        ? this.properties.hashCode()
                        : 0);

    return hash;
  }

  /**
   * @see {@link PropertiesAware#removeProperty(String)}
   *
   *
   * @param key
   */
  @Override
  public void removeProperty(String key)
  {
    properties.remove(key);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * @see {@link PropertiesAware#getProperties()}
   *
   *
   * @return
   */
  @Override
  public Map<String, String> getProperties()
  {
    return properties;
  }

  /**
   * @see {@link PropertiesAware#getProperty(String)}
   *
   *
   * @param key
   *
   * @return
   */
  @Override
  public String getProperty(String key)
  {
    return properties.get(key);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * @see {@link PropertiesAware#setProperties(java.util.Map)}
   *
   *
   * @param properties
   */
  @Override
  public void setProperties(Map<String, String> properties)
  {
    this.properties = properties;
  }

  /**
   * @see {@link PropertiesAware#setProperty(String,String) }
   *
   *
   * @param key
   * @param value
   */
  @Override
  public void setProperty(String key, String value)
  {
    properties.put(key, value);
  }

  //~--- fields ---------------------------------------------------------------

  /** map to hold the properties */
  @XmlJavaTypeAdapter(XmlMapStringAdapter.class)
  protected Map<String, String> properties = new HashMap<String, String>();
}

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


package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Descriptor for available permission objects.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
@XmlRootElement(name = "permission")
@XmlAccessorType(XmlAccessType.FIELD)
public class PermissionDescriptor implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = -9141065458354047154L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructor is only visible for JAXB.
   *
   */
  public PermissionDescriptor() {}

  /**
   * Constructs ...
   *
   *
   * @param displayName
   * @param description
   * @param value
   */
  public PermissionDescriptor(String displayName, String description,
    String value)
  {
    this.displayName = displayName;
    this.description = description;
    this.value = value;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
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

    final PermissionDescriptor other = (PermissionDescriptor) obj;

    return Objects.equal(displayName, other.displayName)
      && Objects.equal(description, other.description)
      && Objects.equal(value, other.value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(displayName, description, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {

    //J-
    return Objects.toStringHelper(this)
                  .add("displayName", displayName)
                  .add("description", description)
                  .add("value", value)
                  .toString();

    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the description of the permission.
   *
   *
   * @return description
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Returns the display name of the permission.
   *
   *
   * @return display name
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * Returns the string representation of the permission.
   *
   *
   * @return string representation
   */
  public String getValue()
  {
    return value;
  }

  //~--- fields ---------------------------------------------------------------

  /** description */
  private String description;

  /** display name */
  @XmlElement(name = "display-name")
  private String displayName;

  /** value */
  private String value;
}

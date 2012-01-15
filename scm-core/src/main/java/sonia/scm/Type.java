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

import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base class for all objects which supports different types.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement
public class Type
{

  /**
   * Constructs {@link Type} object.
   * This constructor is required for JAXB.
   *
   */
  public Type() {}

  /**
   * Constructs {@link Type} object
   *
   *
   * @param name
   * @param displayName
   */
  public Type(String name, String displayName)
  {
    AssertUtil.assertIsNotEmpty(name);
    this.name = name;

    if (Util.isNotEmpty(displayName))
    {
      this.displayName = displayName;
    }
    else
    {
      this.displayName = name;
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns true if the given ovject is equals.
   *
   *
   * @param obj
   *
   * @return true if the given ovject is equals
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

    final Type other = (Type) obj;

    if ((this.name == null)
        ? (other.name != null)
        : !this.name.equals(other.name))
    {
      return false;
    }

    if ((this.displayName == null)
        ? (other.displayName != null)
        : !this.displayName.equals(other.displayName))
    {
      return false;
    }

    return true;
  }

  /**
   * Returns the hash code of the object.
   *
   *
   * @return hash code of the object
   */
  @Override
  public int hashCode()
  {
    int hash = 5;

    hash = 37 * hash + ((this.name != null)
                        ? this.name.hashCode()
                        : 0);
    hash = 37 * hash + ((this.displayName != null)
                        ? this.displayName.hashCode()
                        : 0);

    return hash;
  }

  /**
   * Returns {@link String} representation of the type.
   *
   *
   * @return {@link String} representation of the type
   */
  @Override
  public String toString()
  {
    StringBuilder out = new StringBuilder("Type{name=");

    out.append(name).append(", displayName=").append(displayName).append("}");

    return out.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the display name of the type.
   *
   *
   * @return display name of the type
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * Returns the unique name of the type.
   *
   *
   * @return unique name of the type
   */
  public String getName()
  {
    return name;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Setter for the display name of the type
   *
   *
   *
   * @param displayName
   */
  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  /**
   * Setter for the type name
   *
   *
   * @param name of the type
   */
  public void setName(String name)
  {
    this.name = name;
  }

  //~--- fields ---------------------------------------------------------------

  /** display name of the type */
  private String displayName;

  /** unique name of the type */
  private String name;
}

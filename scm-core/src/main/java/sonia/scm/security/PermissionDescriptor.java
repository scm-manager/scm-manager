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
    
package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

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

  public PermissionDescriptor(String value)
  {
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

    return Objects.equal(value, other.value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return value == null? -1: value.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {

    //J-
    return MoreObjects.toStringHelper(this)
                  .add("value", value)
                  .toString();

    //J+
  }

  //~--- get methods ----------------------------------------------------------

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

  /** value */
  private String value;
}

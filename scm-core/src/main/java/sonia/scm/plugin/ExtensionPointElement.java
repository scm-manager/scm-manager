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
    
package sonia.scm.plugin;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @since 2.0.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "extension-point")
public final class ExtensionPointElement
{
  @XmlElement(name = "class")
  private Class<?> clazz;

  private String description;

  @XmlElement(name = "multi")
  private boolean multiple = true;

  private boolean autoBind = true;

  ExtensionPointElement() {}

  public ExtensionPointElement(Class<?> clazz, String description,
    boolean multiple, boolean autoBind)
  {
    this.clazz = clazz;
    this.description = description;
    this.multiple = multiple;
    this.autoBind = autoBind;
  }

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

    final ExtensionPointElement other = (ExtensionPointElement) obj;

    return Objects.equal(clazz, other.clazz)
      && Objects.equal(description, other.description)
      && Objects.equal(multiple, other.multiple)
      && Objects.equal(autoBind, other.autoBind);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(clazz, description, multiple, autoBind);
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("class", clazz)
                  .add("description", description)
                  .add("multiple", multiple)
                  .add("autoBind", autoBind)
                  .toString();
    //J+
  }


  
  public Class<?> getClazz()
  {
    return clazz;
  }

  
  public String getDescription()
  {
    return description;
  }

  
  public boolean isAutoBind()
  {
    return autoBind;
  }

  
  public boolean isMultiple()
  {
    return multiple;
  }

}

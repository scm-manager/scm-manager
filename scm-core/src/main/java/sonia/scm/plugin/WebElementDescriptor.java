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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.xml.XmlArrayStringAdapter;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Descriptor for web elements such as filter or servlets. A web element can be registered by using the 
 * {@link sonia.scm.filter.WebElement} annotation.
 * 
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@XmlRootElement(name = "web-element")
@XmlAccessorType(XmlAccessType.FIELD)
public final class WebElementDescriptor
{

  /**
   * Constructs ...
   *
   */
  WebElementDescriptor() {}

  /**
   * Constructs ...
   *
   *
   * @param clazz
   * @param pattern
   * @param morePatterns
   * @param regex
   */
  public WebElementDescriptor(Class<?> clazz, String pattern,
    String[] morePatterns, boolean regex)
  {
    this.clazz = clazz;
    this.pattern = pattern;
    this.morePatterns = morePatterns;
    this.regex = regex;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Class<?> getClazz()
  {
    return clazz;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String[] getMorePatterns()
  {
    String[] patterns;

    if (morePatterns != null)
    {
      patterns = Arrays.copyOf(morePatterns, morePatterns.length);
    }
    else
    {
      patterns = new String[0];
    }

    return patterns;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPattern()
  {
    return pattern;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isRegex()
  {
    return regex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clazz, pattern, Arrays.hashCode(morePatterns), regex);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    
    final WebElementDescriptor other = (WebElementDescriptor) obj;
    return Objects.equals(clazz, other.clazz)
      && Objects.equals(pattern, other.pattern)
      && Arrays.equals(morePatterns, other.morePatterns)
      && this.regex == other.regex;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "class")
  private Class<?> clazz;

  /** Field description */
  @XmlJavaTypeAdapter(XmlArrayStringAdapter.class)
  private String[] morePatterns;

  /** Field description */
  @XmlElement(name = "value")
  private String pattern;

  /** Field description */
  private boolean regex = false;
}

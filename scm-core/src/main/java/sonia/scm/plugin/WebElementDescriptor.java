/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "class")
  private Class<?> clazz;

  /** Field description */
  private String[] morePatterns;

  /** Field description */
  @XmlElement(name = "value")
  private String pattern;

  /** Field description */
  private boolean regex = false;
}

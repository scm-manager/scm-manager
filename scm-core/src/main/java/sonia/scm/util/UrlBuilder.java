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



package sonia.scm.util;

//~--- JDK imports ------------------------------------------------------------

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @since 1.9
 * @author Sebastian Sdorra
 */
public class UrlBuilder
{

  /**
   * Constructs ...
   *
   *
   * @param baseUrl
   */
  public UrlBuilder(String baseUrl)
  {
    this.url = baseUrl;

    if (baseUrl.contains(HttpUtil.SEPARATOR_PARAMETERS))
    {
      separator = HttpUtil.SEPARATOR_PARAMETER;
      parameterAdded = true;
    }
    else
    {
      separator = HttpUtil.SEPARATOR_PARAMETERS;
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param part
   *
   * @return
   */
  public UrlBuilder append(String part)
  {
    url = url.concat(part);

    return this;
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param value
   *
   * @return
   */
  public UrlBuilder appendParameter(String name, boolean value)
  {
    return appendParameter(name, String.valueOf(value));
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param value
   *
   * @return
   */
  public UrlBuilder appendParameter(String name, int value)
  {
    return appendParameter(name, String.valueOf(value));
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param value
   *
   * @return
   */
  public UrlBuilder appendParameter(String name, long value)
  {
    return appendParameter(name, String.valueOf(value));
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param value
   *
   * @return
   */
  public UrlBuilder appendParameter(String name, String value)
  {
    if (Util.isNotEmpty(name) && Util.isNotEmpty(value))
    {
      url = new StringBuilder(url).append(separator).append(name).append(
        HttpUtil.SEPARATOR_PARAMETER_VALUE).append(value).toString();
      separator = HttpUtil.SEPARATOR_PARAMETER;
      parameterAdded = true;
    }

    return this;
  }

  /**
   * Method description
   *
   *
   * @param part
   *
   * @return
   */
  public UrlBuilder appendUrlPart(String part)
  {
    if (parameterAdded)
    {
      throw new IllegalStateException("parameter added");
    }

    url = HttpUtil.append(url, part);

    return this;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    return url;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public URL toURL()
  {
    try
    {
      return new URL(url);
    }
    catch (MalformedURLException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean parameterAdded = false;

  /** Field description */
  private String separator;

  /** Field description */
  private String url;
}

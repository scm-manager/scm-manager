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
    
package sonia.scm.util;

import com.google.common.net.UrlEscapers;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @since 1.9
 */
public class UrlBuilder
{
  private boolean parameterAdded = false;

  private String separator;

  private String url;

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

  public UrlBuilder append(String part)
  {
    url = url.concat(part);

    return this;
  }

  public UrlBuilder appendParameter(String name, boolean value)
  {
    return appendParameter(name, String.valueOf(value));
  }

  public UrlBuilder appendParameter(String name, int value)
  {
    return appendParameter(name, String.valueOf(value));
  }

  public UrlBuilder appendParameter(String name, long value)
  {
    return appendParameter(name, String.valueOf(value));
  }

  public UrlBuilder appendParameter(String name, String value)
  {
    if (Util.isNotEmpty(name) && Util.isNotEmpty(value))
    {
      url = new StringBuilder(url)
        .append(separator).append(name)
        .append(HttpUtil.SEPARATOR_PARAMETER_VALUE)
        .append(UrlEscapers.urlFragmentEscaper().escape(value))
        .toString();
      separator = HttpUtil.SEPARATOR_PARAMETER;
      parameterAdded = true;
    }

    return this;
  }

  public UrlBuilder appendUrlPart(String part)
  {
    if (parameterAdded)
    {
      throw new IllegalStateException("parameter added");
    }

    url = HttpUtil.append(url, part);

    return this;
  }

  
  @Override
  public String toString()
  {
    return url;
  }

  
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

}

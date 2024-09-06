/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

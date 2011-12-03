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



package sonia.scm.plugin.rest.url;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.PluginInformation;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.net.MalformedURLException;
import java.net.URL;

import java.text.MessageFormat;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractCompareUrlBuilder implements CompareUrlBuilder
{

  /** the logger for AbstractCompareUrlBuilder */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractCompareUrlBuilder.class);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getServername();

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getUrlPattern();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param latest
   * @param plugin
   * @param other
   *
   * @return
   */
  @Override
  public String createCompareUrl(PluginInformation latest,
                                 PluginInformation plugin,
                                 PluginInformation other)
  {
    return createCompareUrl(latest.getUrl(), plugin.getVersion(),
                            other.getVersion());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   */
  @Override
  public boolean isCompareable(String url)
  {
    return url.contains(getServername());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param urlString
   * @param version
   * @param otherVersion
   *
   * @return
   */
  private String createCompareUrl(String urlString, String version,
                                  String otherVersion)
  {
    String result = null;

    try
    {
      URL url = new URL(urlString);
      String path = url.getPath();

      if (Util.isNotEmpty(path))
      {
        String[] parts = path.split(HttpUtil.SEPARATOR_PATH);

        if (parts.length >= 2)
        {
          result = MessageFormat.format(getUrlPattern(), parts[0], parts[1],
                                        version, otherVersion);
        }
      }
    }
    catch (MalformedURLException ex)
    {
      logger.error("could not parse url", ex);
    }

    return result;
  }
}

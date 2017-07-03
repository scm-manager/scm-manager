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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.google.common.base.Objects;
import com.google.inject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.config.ScmConfigurationChangedEvent;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgHookManager
{

  /** Field description */
  public static final String URL_HOOKPATH = "/hook/hg/";

  /**
   * the logger for HgHookManager
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgHookManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param httpServletRequestProvider
   * @param httpClient
   */
  @Inject
  public HgHookManager(ScmConfiguration configuration,
    Provider<HttpServletRequest> httpServletRequestProvider,
    AdvancedHttpClient httpClient)
  {
    this.configuration = configuration;
    this.httpServletRequestProvider = httpServletRequestProvider;
    this.httpClient = httpClient;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param config
   */
  @Subscribe(async = false)
  public void configChanged(ScmConfigurationChangedEvent config)
  {
    hookUrl = null;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  public String createUrl(HttpServletRequest request)
  {
    if (hookUrl == null)
    {
      synchronized (this)
      {
        if (hookUrl == null)
        {
          buildHookUrl(request);

          if (logger.isInfoEnabled() && Util.isNotEmpty(hookUrl))
          {
            logger.info("use {} for mercurial hooks", hookUrl);
          }
        }
      }
    }

    return hookUrl;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String createUrl()
  {
    String url = hookUrl;

    if (url == null)
    {
      HttpServletRequest request = getHttpServletRequest();

      if (request != null)
      {
        url = createUrl(request);
      }
      else
      {
        url = createConfiguredUrl();
        logger.warn(
          "created url {} without request, in some cases this could cause problems",
          url);
      }
    }

    return url;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getChallenge()
  {
    return challenge;
  }

  /**
   * Method description
   *
   *
   * @param challenge
   *
   * @return
   */
  public boolean isAcceptAble(String challenge)
  {
    return this.challenge.equals(challenge);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   */
  private void buildHookUrl(HttpServletRequest request)
  {
    if (configuration.isForceBaseUrl())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug(
          "create hook url from configured base url because force base url is enabled");
      }

      hookUrl = createConfiguredUrl();

      if (!isUrlWorking(hookUrl))
      {
        disableHooks();
      }
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("create hook url from request");
      }

      hookUrl = HttpUtil.getCompleteUrl(request, URL_HOOKPATH);

      if (!isUrlWorking(hookUrl))
      {
        if (logger.isWarnEnabled())
        {
          logger.warn(
            "hook url {} from request does not work, try now localhost",
            hookUrl);
        }

        hookUrl = createLocalUrl(request);

        if (!isUrlWorking(hookUrl))
        {
          if (logger.isWarnEnabled())
          {
            logger.warn(
              "localhost hook url {} does not work, try now from configured base url",
              hookUrl);
          }

          hookUrl = createConfiguredUrl();

          if (!isUrlWorking(hookUrl))
          {
            disableHooks();
          }
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String createConfiguredUrl()
  {
    //J-
    return HttpUtil.getUriWithoutEndSeperator(
      Objects.firstNonNull(
        configuration.getBaseUrl(), 
        "http://localhost:8080/scm"
      )
    ).concat("/hook/hg/");
    //J+
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  private String createLocalUrl(HttpServletRequest request)
  {
    StringBuilder sb = new StringBuilder(request.getScheme());

    sb.append("://localhost:").append(request.getLocalPort());
    sb.append(request.getContextPath()).append(URL_HOOKPATH);

    return sb.toString();
  }

  /**
   * Method description
   *
   */
  private void disableHooks()
  {
    if (logger.isErrorEnabled())
    {
      logger.error(
        "disabling mercurial hooks, because hook url {} seems not to work",
        hookUrl);
    }

    hookUrl = Util.EMPTY_STRING;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private HttpServletRequest getHttpServletRequest()
  {
    HttpServletRequest request = null;

    try
    {
      request = httpServletRequestProvider.get();
    }
    catch (ProvisionException | OutOfScopeException ex)
    {
      logger.debug("http servlet request is not available");
    }

    return request;
  }

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   */
  private boolean isUrlWorking(String url)
  {
    boolean result = false;

    try
    {
      url = url.concat("?ping=true");

      logger.trace("check hook url {}", url);
      //J-
      int sc = httpClient.get(url)
                         .disableHostnameValidation(true)
                         .disableCertificateValidation(true)
                         .ignoreProxySettings(true)
                         .request()
                         .getStatus();
      //J+
      result = sc == 204;
    }
    catch (IOException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("url test failed for url ".concat(url), ex);
      }
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String challenge = UUID.randomUUID().toString();

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private volatile String hookUrl;

  /** Field description */
  private AdvancedHttpClient httpClient;

  /** Field description */
  private Provider<HttpServletRequest> httpServletRequestProvider;
}

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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.config.ScmConfigurationChangedEvent;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.UUID;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgHookManager {

  @SuppressWarnings("java:S1075") // this url is fixed
  private static final String URL_HOOKPATH = "/hook/hg/";

  /**
   * the logger for HgHookManager
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HgHookManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *  @param configuration
   * @param httpServletRequestProvider
   * @param httpClient
   * @param accessTokenBuilderFactory
   */
  @Inject
  public HgHookManager(ScmConfiguration configuration,
                       Provider<HttpServletRequest> httpServletRequestProvider,
                       AdvancedHttpClient httpClient, AccessTokenBuilderFactory accessTokenBuilderFactory)
  {
    this.configuration = configuration;
    this.httpServletRequestProvider = httpServletRequestProvider;
    this.httpClient = httpClient;
    this.accessTokenBuilderFactory = accessTokenBuilderFactory;
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

  public AccessToken getAccessToken()
  {
    return accessTokenBuilderFactory.create().build();
  }

  private void buildHookUrl(HttpServletRequest request) {
    if (configuration.isForceBaseUrl()) {
      logger.debug("create hook url from configured base url because force base url is enabled");

      hookUrl = createConfiguredUrl();
      if (!isUrlWorking(hookUrl)) {
        disableHooks();
      }
    } else {
      logger.debug("create hook url from request");

      hookUrl = HttpUtil.getCompleteUrl(request, URL_HOOKPATH);
      if (!isUrlWorking(hookUrl)) {
        logger.warn("hook url {} from request does not work, try now localhost", hookUrl);

        hookUrl = createLocalUrl(request);
        if (!isUrlWorking(hookUrl)) {
          logger.warn("localhost hook url {} does not work, try now from configured base url", hookUrl);

          hookUrl = createConfiguredUrl();
          if (!isUrlWorking(hookUrl)) {
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
      MoreObjects.firstNonNull(
        configuration.getBaseUrl(),
        "http://localhost:8080/scm"
      )
    ).concat(URL_HOOKPATH);
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

  private boolean isUrlWorking(String url) {
    boolean result = false;

    try {
      String pingChallenge = UUID.randomUUID().toString();
      url = url.concat("?ping=true&challenge=").concat(pingChallenge);

      logger.trace("check hook url {}", url);
      AdvancedHttpResponse response = httpClient.get(url)
         .disableHostnameValidation(true)
         .disableCertificateValidation(true)
         .ignoreProxySettings(true)
         .disableTracing()
         .request();

      if (response.isSuccessful()) {
        String signature = response.contentAsString();
        if (verify(pingChallenge, signature)) {
          result = true;
        } else {
          logger.warn("hook callback {} returned wrong challenge", url);
        }
      }
    }
    catch (IOException ex) {
      logger.trace("url test failed for url {}", url, ex);
    }
    return result;
  }

  @SuppressWarnings("UnstableApiUsage")
  public String sign(String content) {
    return Hashing.hmacSha256(signingKey)
      .hashString(content, StandardCharsets.UTF_8)
      .toString();
  }

  public boolean verify(String content, String signature) {
    return sign(content).equals(signature);
  }

  private byte[] createSigningKey() {
    SecureRandom random = new SecureRandom();
    byte[] data = new byte[64];
    random.nextBytes(data);
    return data;
  }

  public boolean isHookUrlConfigured() {
    return !Strings.isNullOrEmpty(hookUrl);
  }

  /** Field description */
  private final String challenge = UUID.randomUUID().toString();

  private final byte[] signingKey = createSigningKey();

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private volatile String hookUrl;

  /** Field description */
  private AdvancedHttpClient httpClient;

  /** Field description */
  private Provider<HttpServletRequest> httpServletRequestProvider;

  private final AccessTokenBuilderFactory accessTokenBuilderFactory;
}

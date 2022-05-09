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

package sonia.scm;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.OutOfScopeException;
import com.google.inject.ProvisionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Default implementation of {@link RootURL}.
 *
 * @since 2.3.1
 */
public class DefaultRootURL implements RootURL {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultRootURL.class);

  private final Provider<HttpServletRequest> requestProvider;
  private final ScmConfiguration configuration;

  private final LoadingCache<String, URL> urlCache;

  @Inject
  public DefaultRootURL(Provider<HttpServletRequest> requestProvider, ScmConfiguration configuration) {
    this(requestProvider, configuration, new UrlFromString());
  }

  @VisibleForTesting
  DefaultRootURL(Provider<HttpServletRequest> requestProvider, ScmConfiguration configuration, UrlFromString cacheLoader) {
    this.requestProvider = requestProvider;
    this.configuration = configuration;
    this.urlCache = CacheBuilder.newBuilder().maximumSize(10).build(cacheLoader);
  }

  @Override
  public URL get() {
    String url = fromRequest().orElseGet(configuration::getBaseUrl);
    if (url == null) {
      throw new IllegalStateException("The configured base url is empty. This can only happened if SCM-Manager has not received any requests.");
    }
    try {
      return urlCache.get(url);
    } catch (ExecutionException e) {
      throw new IllegalStateException(String.format("base url \"%s\" is malformed", url), e);
    }
  }

  private Optional<String> fromRequest() {
    try {
      HttpServletRequest request = requestProvider.get();
      return Optional.of(HttpUtil.getCompleteUrl(request));
    } catch (ProvisionException ex) {
      if (ex.getCause() instanceof OutOfScopeException) {
        LOG.debug("could not find request, fall back to base url from configuration");
        return Optional.empty();
      }
      throw ex;
    }
  }

  @VisibleForTesting
  static class UrlFromString extends CacheLoader<String, URL> {
    @Override
    public URL load(String urlString) throws MalformedURLException {
      URL url = new URL(urlString);
      if (url.getPort() == url.getDefaultPort()) {
        return new URL(url.getProtocol(), url.getHost(), -1, url.getFile());
      }
      return url;
    }
  }
}

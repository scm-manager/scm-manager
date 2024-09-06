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

package sonia.scm;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.OutOfScopeException;
import com.google.inject.ProvisionException;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;

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

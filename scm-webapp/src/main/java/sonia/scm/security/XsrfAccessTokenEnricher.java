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

package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.OutOfScopeException;
import com.google.inject.ProvisionException;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.util.HttpUtil;

import java.util.UUID;

/**
 * Xsrf access token enricher will add an xsrf custom field to the access token. The enricher will only
 * add the xsrf field, if the authentication request is issued from the web interface and xsrf protection is
 * enabled. The xsrf field will be validated on every request by the {@link XsrfAccessTokenValidator}. Xsrf protection
 * can be disabled with {@link ScmConfiguration#setEnabledXsrfProtection(boolean)}.
 *
 * @see <a href="https://goo.gl/s67xO3">Issue 793</a>
 * @since 2.0.0
 */
@Extension
public class XsrfAccessTokenEnricher implements AccessTokenEnricher {

 
  private static final Logger LOG = LoggerFactory.getLogger(XsrfAccessTokenEnricher.class);

  private final ScmConfiguration configuration;
  private final Provider<HttpServletRequest> requestProvider;

  /**
   * Constructs a new instance.
   *
   * @param configuration   scm main configuration
   * @param requestProvider http request provider
   */
  @Inject
  public XsrfAccessTokenEnricher(ScmConfiguration configuration, Provider<HttpServletRequest> requestProvider) {
    this.configuration = configuration;
    this.requestProvider = requestProvider;
  }

  @Override
  public void enrich(AccessTokenBuilder builder) {
    if (configuration.isEnabledXsrfProtection()) {
      if (isEnrichable()) {
        builder.custom(Xsrf.TOKEN_KEY, createToken());
      }
    } else {
      LOG.trace("xsrf is disabled, skip xsrf enrichment");
    }
  }

  private boolean isEnrichable() {
    try {
      HttpServletRequest request = requestProvider.get();
      if (HttpUtil.isWUIRequest(request)) {
        LOG.debug("received wui token claim, enrich jwt with xsrf key");
        return true;
      } else {
        LOG.trace("skip xsrf enrichment, because jwt session is started from a non wui client");
        return false;
      }
    } catch (ProvisionException ex) {
      if (ex.getCause() instanceof OutOfScopeException) {
        LOG.trace("skip xsrf enrichment, because no request scope is available");
        return false;
      } else {
        throw ex;
      }
    }
  }

  @VisibleForTesting
  String createToken() {
    // TODO create interface and use a better method
    return UUID.randomUUID().toString();
  }

}

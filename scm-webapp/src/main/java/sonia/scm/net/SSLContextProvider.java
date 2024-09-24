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

package sonia.scm.net;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

/**
 * Provider for {@link SSLContext}. The provider will first try to retrieve the {@link SSLContext} from an "default"
 * named optional provider, if this fails the provider will return the jvm default context.
 *
 * @version 1.47
 */
public final class SSLContextProvider implements Provider<SSLContext> {

 
  private static final Logger logger = LoggerFactory.getLogger(SSLContextProvider.class);

  @Named("default")
  @Inject(optional = true)
  private Provider<SSLContext> sslContextProvider;

  @Override
  public SSLContext get() {
    SSLContext context = null;
    if (sslContextProvider != null) {
      context = sslContextProvider.get();
    }

    if (context == null) {
      try {
        logger.trace("could not find ssl context provider, use jvm default");
        context = SSLContext.getDefault();
      } catch (NoSuchAlgorithmException ex) {
        throw Throwables.propagate(ex);
      }
    } else {
      logger.trace("use custom ssl context from provider");
    }
    return context;
  }

}

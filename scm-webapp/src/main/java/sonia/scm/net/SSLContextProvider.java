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
 * @author Sebastian Sdorra
 * @version 1.47
 */
public final class SSLContextProvider implements Provider<SSLContext> {

  /**
   * the logger for SSLContextProvider
   */
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

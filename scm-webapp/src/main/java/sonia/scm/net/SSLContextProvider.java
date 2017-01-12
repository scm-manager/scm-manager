/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.net;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import java.security.NoSuchAlgorithmException;
import javax.inject.Named;
import javax.inject.Provider;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for {@link SSLContext}. The provider will first try to retrieve the {@link SSLContext} from an "default"
 * named optional provider, if this fails the provider will return the jvm default context.
 *
 * @author Sebastian Sdorra
 * @version 1.47
 */
public final class SSLContextProvider implements Provider<SSLContext>
{

  /**
   * the logger for SSLContextProvider
   */
  private static final Logger logger = LoggerFactory.getLogger(SSLContextProvider.class);

  @Named("default")
  @Inject(optional = true)
  private Provider<SSLContext> sslContextProvider;

  @Override
  public SSLContext get()
  {
    SSLContext context = null;
    if (sslContextProvider != null)
    {
      context = sslContextProvider.get();
    }

    if (context == null)
    {
      try
      {
        logger.trace("could not find ssl context provider, use jvm default");
        context = SSLContext.getDefault();
      }
      catch (NoSuchAlgorithmException ex)
      {
        throw Throwables.propagate(ex);
      }
    } 
    else 
    {
      logger.trace("use custom ssl context from provider");
    }
    return context;
  }

}

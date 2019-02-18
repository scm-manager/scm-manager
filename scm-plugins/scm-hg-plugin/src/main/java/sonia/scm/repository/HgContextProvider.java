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


import com.google.common.annotations.VisibleForTesting;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Injection provider for {@link HgContext}.
 * This provider returns an instance {@link HgContext} from request scope, if no {@link HgContext} could be found in
 * request scope (mostly because the scope is not available) a new {@link HgContext} gets returned.
 *
 * @author Sebastian Sdorra
 */
public class HgContextProvider implements Provider<HgContext>
{

  /**
   * the LOG for HgContextProvider
   */
  private static final Logger LOG =
    LoggerFactory.getLogger(HgContextProvider.class);

  //~--- get methods ----------------------------------------------------------

  private Provider<HgContextRequestStore> requestStoreProvider;

  @Inject
  public HgContextProvider(Provider<HgContextRequestStore> requestStoreProvider) {
    this.requestStoreProvider = requestStoreProvider;
  }

  @VisibleForTesting
  public HgContextProvider() {
  }

  @Override
  public HgContext get() {
    HgContext context = fetchContextFromRequest();
    if (context != null) {
      LOG.trace("return HgContext from request store");
      return context;
    }
    LOG.trace("could not find context in request scope, returning new instance");
    return new HgContext();
  }

  private HgContext fetchContextFromRequest() {
    try {
      if (requestStoreProvider != null) {
        return requestStoreProvider.get().get();
      } else {
        LOG.trace("no request store provider defined, could not return context from request");
        return null;
      }
    } catch (ProvisionException ex) {
      if (ex.getCause() instanceof OutOfScopeException) {
        LOG.trace("we are currently out of request scope, failed to retrieve context");
        return null;
      } else {
        throw ex;
      }
    }
  }
}

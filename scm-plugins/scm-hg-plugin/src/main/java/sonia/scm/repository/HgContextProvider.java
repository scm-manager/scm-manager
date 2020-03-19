/**
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

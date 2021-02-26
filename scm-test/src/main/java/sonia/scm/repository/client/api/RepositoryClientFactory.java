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

package sonia.scm.repository.client.api;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import sonia.scm.repository.client.spi.RepositoryClientFactoryProvider;
import sonia.scm.util.ServiceUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 * @author Sebastian Sdorra
 * @since 1.18
 */
public final class RepositoryClientFactory {

  /**
   * Constructs ...
   */
  public RepositoryClientFactory() {
    this.providers =
      ServiceUtil.getServices(RepositoryClientFactoryProvider.class);
  }

  /**
   * Constructs ...
   *
   * @param providers
   */
  public RepositoryClientFactory(
    Iterable<RepositoryClientFactoryProvider> providers) {
    this.providers = providers;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param type
   * @param main
   * @param workingCopy
   * @return
   * @throws IOException
   */
  public RepositoryClient create(String type, File main, File workingCopy)
    throws IOException {

    return new RepositoryClient(getProvider(type).create(main, workingCopy));
  }

  /**
   * Method description
   *
   * @param type
   * @param url
   * @param username
   * @param password
   * @param workingCopy
   * @return
   * @throws IOException
   */
  public RepositoryClient create(String type, String url, String username,
                                 String password, File workingCopy)
    throws IOException {
    return new RepositoryClient(getProvider(type).create(url, username,
      password, workingCopy));
  }

  public RepositoryClient create(String type, String url, File workingCopy)
    throws IOException {
    return new RepositoryClient(getProvider(type).create(url, null, null, workingCopy));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @return
   */
  public Iterable<String> getAvailableTypes() {
    List<String> types = Lists.newArrayList();

    for (RepositoryClientFactoryProvider provider : providers) {
      types.add(provider.getType());
    }

    return types;
  }

  /**
   * Method description
   *
   * @param type
   * @return
   */
  private RepositoryClientFactoryProvider getProvider(String type) {
    RepositoryClientFactoryProvider provider = null;

    for (RepositoryClientFactoryProvider p : providers) {
      if (p.getType().equalsIgnoreCase(type)) {
        provider = p;

        break;
      }
    }

    if (provider == null) {
      throw new RuntimeException(
        "could not find provider for type ".concat(type));
    }

    return provider;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Field description
   */
  private Iterable<RepositoryClientFactoryProvider> providers;
}

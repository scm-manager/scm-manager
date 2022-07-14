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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.security.CipherUtil;
import sonia.scm.user.User;
import sonia.scm.util.ServiceUtil;

/**
 * The SCMContext searches a implementation of {@link SCMContextProvider} and
 * holds a singleton instance of this implementation.
 *
 * @author Sebastian Sdorra
 */
public final class SCMContext {

  /**
   * Default java package for finding extensions
   */
  public static final String DEFAULT_PACKAGE = "sonia.scm";

  /**
   * Name of the anonymous user
   */
  public static final String USER_ANONYMOUS = "_anonymous";

  /**
   * the anonymous user
   *
   * @since 1.21
   */
  public static final User ANONYMOUS = new User(
    USER_ANONYMOUS,
    "SCM Anonymous",
    "scm-anonymous@scm-manager.org",
    CipherUtil.getInstance().encode("__not_necessary_password__"),
    "xml",
    true
  );

  /**
   * Singleton instance of {@link SCMContextProvider}
   */
  private static SCMContextProvider provider = null;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   */
  private SCMContext() {
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the singleton instance of {@link SCMContextProvider}
   *
   * @return singleton instance of {@link SCMContextProvider}
   */
  public static SCMContextProvider getContext() {
    synchronized (SCMContext.class) {
      if (provider == null) {
        provider = ServiceUtil.getService(SCMContextProvider.class);

        if (provider == null) {
          provider = new BasicContextProvider();
        }
      }
    }

    return provider;
  }
}

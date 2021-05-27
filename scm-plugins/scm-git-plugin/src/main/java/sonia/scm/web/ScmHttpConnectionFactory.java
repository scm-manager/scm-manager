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

package sonia.scm.web;

import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.transport.http.HttpConnection;
import org.eclipse.jgit.transport.http.JDKHttpConnection;
import org.eclipse.jgit.transport.http.JDKHttpConnectionFactory;
import org.eclipse.jgit.transport.http.NoCheckX509TrustManager;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;

public class ScmHttpConnectionFactory extends JDKHttpConnectionFactory {

  private final Provider<TrustManager> trustManagerProvider;
  private final KeyManager[] keyManagers;

  @Inject
  public ScmHttpConnectionFactory(Provider<TrustManager> trustManagerProvider) {
    this(trustManagerProvider, null);
  }

  public ScmHttpConnectionFactory(Provider<TrustManager> trustManagerProvider, KeyManager[] keyManagers) {
    this.trustManagerProvider = trustManagerProvider;
    this.keyManagers = keyManagers;
  }

  @Override
  public GitSession newSession() {
    return new ScmConnectionSession(trustManagerProvider.get(), keyManagers);
  }

  private static class ScmConnectionSession implements GitSession {

    private final TrustManager trustManager;
    private final KeyManager[] keyManagers;

    private ScmConnectionSession(TrustManager trustManager, KeyManager[] keyManagers) {
      this.trustManager = trustManager;
      this.keyManagers = keyManagers;
    }

    @Override
    @SuppressWarnings("java:S5527")
    public JDKHttpConnection configure(HttpConnection connection,
                                       boolean sslVerify) throws GeneralSecurityException {
      if (!(connection instanceof JDKHttpConnection)) {
        throw new IllegalArgumentException(MessageFormat.format(
          JGitText.get().httpWrongConnectionType,
          JDKHttpConnection.class.getName(),
          connection.getClass().getName()));
      }
      JDKHttpConnection conn = (JDKHttpConnection) connection;
      String scheme = conn.getURL().getProtocol();
      if ("https".equals(scheme) && sslVerify) { //$NON-NLS-1$
        // sslVerify == true: use the JDK defaults
        conn.configure(keyManagers, new TrustManager[]{trustManager}, null);
      } else if ("https".equals(scheme)) {
        conn.configure(keyManagers, new TrustManager[]{new NoCheckX509TrustManager()}, null);
        conn.setHostnameVerifier((name, value) -> true);
      }

      return conn;
    }

    @Override
    public void close() {
      // Nothing
    }
  }
}

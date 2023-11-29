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

import jakarta.inject.Inject;
import org.eclipse.jgit.transport.http.HttpConnection;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.eclipse.jgit.transport.http.WrappedHttpUrlConnection;
import sonia.scm.net.HttpConnectionOptions;
import sonia.scm.net.HttpURLConnectionFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

public class ScmHttpConnectionFactory implements HttpConnectionFactory {

  private final HttpURLConnectionFactory connectionFactory;
  private final HttpConnectionOptions options;

  @Inject
  public ScmHttpConnectionFactory(HttpURLConnectionFactory connectionFactory) {
    this(connectionFactory, new HttpConnectionOptions());
  }

  public ScmHttpConnectionFactory(HttpURLConnectionFactory connectionFactory, HttpConnectionOptions options) {
    this.connectionFactory = connectionFactory;
    this.options = options;
  }

  @Override
  public HttpConnection create(URL url) throws IOException {
    return new WrappedHttpUrlConnection(connectionFactory.create(url, options));
  }

  @Override
  public HttpConnection create(URL url, Proxy proxy) throws IOException {
    // we ignore proxy configuration of jgit, because we have our own
    return create(url);
  }
}

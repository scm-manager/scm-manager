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

package sonia.scm.repository.spi;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import sonia.scm.net.HttpConnectionOptions;
import sonia.scm.net.HttpURLConnectionFactory;
import sonia.scm.web.ScmHttpConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class PullHttpConnectionProvider {

  private final HttpURLConnectionFactory httpURLConnectionFactory;

  @Inject
  PullHttpConnectionProvider(HttpURLConnectionFactory httpURLConnectionFactory) {
    this.httpURLConnectionFactory = httpURLConnectionFactory;
  }

  HttpConnectionFactory createHttpConnectionFactory(PullCommandRequest request) {
    HttpConnectionOptions options = new HttpConnectionOptions();
    if (!Strings.isNullOrEmpty(request.getUsername()) && !Strings.isNullOrEmpty(request.getPassword())) {
      String encodedAuth = Base64.getEncoder().encodeToString((request.getUsername() + ":" + request.getPassword()).getBytes(StandardCharsets.UTF_8));
      String authHeaderValue = "Basic " + encodedAuth;
      options.addRequestProperty("Authorization", authHeaderValue);
    }
    return new ScmHttpConnectionFactory(httpURLConnectionFactory, options);
  }
}

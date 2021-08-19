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

import org.eclipse.jgit.transport.http.HttpConnection;
import org.eclipse.jgit.transport.http.JDKHttpConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.HttpConnectionOptions;
import sonia.scm.net.HttpURLConnectionFactory;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScmHttpConnectionFactoryTest {

  @Mock
  private HttpURLConnectionFactory internalConnectionFactory;

  @Captor
  private ArgumentCaptor<HttpConnectionOptions> connectionOptionsCaptor;

  @Test
  void shouldCreateConnection() throws IOException {
    ScmHttpConnectionFactory connectionFactory = new ScmHttpConnectionFactory(internalConnectionFactory);

    URL url = new URL("https://scm.hitchhiker.org");
    HttpConnection httpConnection = connectionFactory.create(url, null);

    assertThat(httpConnection)
      .isNotNull()
      .isInstanceOf(JDKHttpConnection.class);
    verify(internalConnectionFactory).create(eq(url), connectionOptionsCaptor.capture());
    assertThat(connectionOptionsCaptor.getValue()).isNotNull();
  }

  @Test
  void shouldCreateConnectionWithOptions() throws IOException {
    HttpConnectionOptions options = new HttpConnectionOptions();
    ScmHttpConnectionFactory connectionFactory = new ScmHttpConnectionFactory(internalConnectionFactory, options);

    URL url = new URL("https://scm.hitchhiker.org");
    HttpConnection httpConnection = connectionFactory.create(url);

    assertThat(httpConnection)
      .isNotNull()
      .isInstanceOf(JDKHttpConnection.class);
    verify(internalConnectionFactory).create(url, options);
  }
}

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

import org.eclipse.jgit.transport.http.HttpConnection;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.HttpConnectionOptions;
import sonia.scm.net.HttpURLConnectionFactory;
import sonia.scm.net.ProxyConfiguration;
import sonia.scm.repository.api.SimpleUsernamePasswordCredential;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MirrorHttpConnectionProviderTest {

  @Mock
  private HttpURLConnectionFactory internalConnectionFactory;

  @InjectMocks
  private MirrorHttpConnectionProvider provider;

  @Captor
  private ArgumentCaptor<HttpConnectionOptions> captor;

  @Test
  void shouldNotConfigureProxy() throws IOException {
    MirrorCommandRequest request = new MirrorCommandRequest();

    HttpConnectionOptions value = create(request);

    assertThat(value.getProxyConfiguration()).isEmpty();
  }

  @Test
  void shouldConfigureProxy() throws IOException {
    ProxyConfiguration proxy = mock(ProxyConfiguration.class);
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setProxyConfiguration(proxy);

    HttpConnectionOptions value = create(request);

    assertThat(value.getProxyConfiguration()).containsSame(proxy);
  }

  @Test
  void shouldConfigureAuthorizationHeader() throws IOException {
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setCredentials(List.of(new SimpleUsernamePasswordCredential("dent", "yellow".toCharArray())));

    HttpConnectionOptions value = create(request);

    assertThat(value.getConnectionProperties()).containsEntry("Authorization", "Basic ZGVudDp5ZWxsb3c=");
  }

  @Test
  void shouldSetUserAgentHeader() throws IOException {
    MirrorCommandRequest request = new MirrorCommandRequest();

    HttpConnectionOptions value = create(request);

    assertThat(value.getConnectionProperties()).containsEntry("User-Agent", "git-lfs/2");
  }

  private HttpConnectionOptions create(MirrorCommandRequest request) throws IOException {
    List<String> log = new ArrayList<>();

    HttpConnectionFactory connectionFactory = provider.createHttpConnectionFactory(request, log);
    assertThat(connectionFactory).isNotNull();

    HttpConnection connection = connectionFactory.create(new URL("https://hitchhiker.com"));
    assertThat(connection).isNotNull();

    verify(internalConnectionFactory).create(any(), captor.capture());
    return captor.getValue();
  }

}

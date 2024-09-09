/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

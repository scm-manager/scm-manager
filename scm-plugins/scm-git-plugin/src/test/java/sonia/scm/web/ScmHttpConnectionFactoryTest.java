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

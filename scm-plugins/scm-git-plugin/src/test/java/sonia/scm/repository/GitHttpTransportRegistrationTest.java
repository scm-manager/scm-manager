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

package sonia.scm.repository;

import org.eclipse.jgit.transport.HttpTransport;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.web.ScmHttpConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GitHttpTransportRegistrationTest {

  @Mock
  private ScmHttpConnectionFactory scmHttpConnectionFactory;

  private HttpConnectionFactory capturedFactory;

  @BeforeEach
  void captureConnectionFactory() {
    this.capturedFactory = HttpTransport.getConnectionFactory();
  }

  @AfterEach
  void restoreConnectionFactory() {
    HttpTransport.setConnectionFactory(capturedFactory);
  }

  @Test
  void shouldSetHttpConnectionFactory() {
    new GitHttpTransportRegistration(scmHttpConnectionFactory).contextInitialized(null);
    assertThat(HttpTransport.getConnectionFactory()).isSameAs(scmHttpConnectionFactory);
  }

}

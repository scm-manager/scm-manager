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

package sonia.scm.security;

import jakarta.servlet.http.HttpServletRequest;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HealthCheckResource;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class DefaultShouldRequestPassCheckerTest {

  @Mock
  private HttpServletRequest request;
  @Mock
  private UserAgentParser userAgentParser;
  private ScmConfiguration configuration;
  private DefaultShouldRequestPassChecker checker;

  @BeforeEach
  void setup() {
    lenient().when(request.getContextPath()).thenReturn("/scm");
    lenient().when(request.getRequestURI()).thenReturn("/scm/random/uri");
    configuration = new ScmConfiguration();
    checker = new DefaultShouldRequestPassChecker(configuration, userAgentParser);
  }

  @Test
  @SubjectAware("Trainer Red")
  void shouldPassBecauseUserIsAuthenticated() {
    assertThat(checker.shouldPass(request)).isTrue();
  }

  @Test
  @SubjectAware(Authentications.PRINCIPAL_ANONYMOUS)
  void shouldNotPassBecauseUserIsAnonymous() {
    assertThat(checker.shouldPass(request)).isFalse();
  }

  @Test
  @SubjectAware(Authentications.PRINCIPAL_ANONYMOUS)
  void shouldPassBecauseAnonymousProtocolRequestIsEnabled() {
    configuration.setAnonymousMode(AnonymousMode.PROTOCOL_ONLY);
    when(userAgentParser.parse(request)).thenReturn(UserAgent.other("git").build());
    assertThat(checker.shouldPass(request)).isTrue();
  }

  @Test
  @SubjectAware(Authentications.PRINCIPAL_ANONYMOUS)
  void shouldPassBecauseMercurialHookRequest() {
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestURI()).thenReturn("/scm/hook/hg/");
    assertThat(checker.shouldPass(request)).isTrue();
  }

  @Test
  @SubjectAware(Authentications.PRINCIPAL_ANONYMOUS)
  void shouldPassBecauseFullAnonymousAccessIsEnabled() {
    configuration.setAnonymousMode(AnonymousMode.FULL);
    assertThat(checker.shouldPass(request)).isTrue();
  }

  @Test
  @SubjectAware(Authentications.PRINCIPAL_ANONYMOUS)
  void shouldPassBecauseRequestIsHealthCheck() {
    when(request.getRequestURI()).thenReturn("/scm/api/" + HealthCheckResource.PATH);
    assertThat(checker.shouldPass(request)).isTrue();
  }
}

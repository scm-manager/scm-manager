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
    
package sonia.scm.repository;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessToken;
import sonia.scm.security.Xsrf;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgEnvironmentTest {

  @Mock
  HgRepositoryHandler handler;
  @Mock
  HgHookManager hookManager;

  @Test
  void shouldExtractXsrfTokenWhenSet() {
    AccessToken accessToken = mock(AccessToken.class);
    when(accessToken.compact()).thenReturn("");
    when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(of("XSRF Token"));
    when(hookManager.getAccessToken()).thenReturn(accessToken);

    Map<String, String> environment = new HashMap<>();
    HgEnvironment.prepareEnvironment(environment, handler, hookManager);

    assertThat(environment).contains(entry("SCM_XSRF", "XSRF Token"));
  }

  @Test
  void shouldIgnoreXsrfWhenNotSetButStillContainDummy() {
    AccessToken accessToken = mock(AccessToken.class);
    when(accessToken.compact()).thenReturn("");
    when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(empty());
    when(hookManager.getAccessToken()).thenReturn(accessToken);

    Map<String, String> environment = new HashMap<>();
    HgEnvironment.prepareEnvironment(environment, handler, hookManager);

    assertThat(environment).containsKeys("SCM_XSRF");
  }
}

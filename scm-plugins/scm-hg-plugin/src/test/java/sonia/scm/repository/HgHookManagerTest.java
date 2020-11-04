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

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.security.AccessTokenBuilderFactory;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HgHookManagerTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private AdvancedHttpClient httpClient;

  @Mock
  private AccessTokenBuilderFactory accessTokenBuilderFactory;

  private HgHookManager hookManager;

  @BeforeEach
  void setUpObjectUnderTest() {
    hookManager = new HgHookManager(
      new ScmConfiguration(), Providers.of(request), httpClient, accessTokenBuilderFactory
    );
  }

  @Test
  void shouldReturnSignature() {
    String signature = hookManager.sign("hello");
    System.out.println(signature);
    assertThat(signature).isNotEqualTo("hello");
  }

  @Test
  void shouldReturnTrueForValidSignature() {
    String signature = hookManager.sign("challenge");
    assertThat(hookManager.verify("challenge", signature)).isTrue();
  }

  @Test
  void shouldReturnForInvalidSignature() {
    String signature = hookManager.sign("challenge");
    assertThat(hookManager.verify("other", signature)).isFalse();
  }

}

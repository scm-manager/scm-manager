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

package sonia.scm;

import com.google.inject.util.Providers;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.template.TemplateEngine;

@RunWith(MockitoJUnitRunner.class)
public class PushStateDispatcherProviderTest {

  @Mock
  private TemplateEngine templateEngine;

  @Mock
  private SCMContextProvider context;

  private PushStateDispatcherProvider provider = new PushStateDispatcherProvider(
    Providers.of(new TemplatingPushStateDispatcher(templateEngine, context))
  );

  @Test
  public void testGetProxyPushStateWithPropertySet() {
    System.setProperty(PushStateDispatcherProvider.PROPERTY_TARGET, "http://localhost:9966");
    PushStateDispatcher dispatcher = provider.get();
    Assertions.assertThat(dispatcher).isInstanceOf(ProxyPushStateDispatcher.class);
  }

  @Test
  public void testGetProxyPushStateWithoutProperty() {
    PushStateDispatcher dispatcher = provider.get();
    Assertions.assertThat(dispatcher).isInstanceOf(TemplatingPushStateDispatcher.class);
  }

  @After
  public void cleanupSystemProperty() {
    System.clearProperty(PushStateDispatcherProvider.PROPERTY_TARGET);
  }

}

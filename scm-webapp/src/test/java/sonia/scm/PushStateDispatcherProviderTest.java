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

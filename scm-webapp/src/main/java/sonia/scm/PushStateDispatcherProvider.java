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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Injection Provider for the {@link PushStateDispatcher}. The provider will return a {@link ProxyPushStateDispatcher}
 * if the system property {@code PushStateDispatcherProvider#PROPERTY_TARGET} is set to a proxy target url, otherwise
 * a {@link TemplatingPushStateDispatcher} is used.
 *
 * @since 2.0.0
 */
public class PushStateDispatcherProvider implements Provider<PushStateDispatcher> {

  @VisibleForTesting
  static final String PROPERTY_TARGET = "sonia.scm.ui.proxy";

  private Provider<TemplatingPushStateDispatcher> templatingPushStateDispatcherProvider;

  @Inject
  public PushStateDispatcherProvider(Provider<TemplatingPushStateDispatcher> templatingPushStateDispatcherProvider) {
    this.templatingPushStateDispatcherProvider = templatingPushStateDispatcherProvider;
  }

  @Override
  public PushStateDispatcher get() {
    String target = System.getProperty(PROPERTY_TARGET);
    if (Strings.isNullOrEmpty(target)) {
      return templatingPushStateDispatcherProvider.get();
    }
    return new ProxyPushStateDispatcher(target);
  }
}

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

package sonia.scm.schedule;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.web.security.AdministrationContext;

class PrivilegedRunnableFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PrivilegedRunnableFactory.class);

  private final AdministrationContext context;

  @Inject
  PrivilegedRunnableFactory(AdministrationContext context) {
    this.context = context;
  }

  public Runnable create(Provider<? extends Runnable> runnableProvider) {
    return () -> context.runAsAdmin(() -> {
      LOG.trace("create runnable from provider");
      Runnable runnable = runnableProvider.get();
      LOG.debug("execute scheduled job {}", runnable.getClass());
      runnable.run();
    });
  }
}

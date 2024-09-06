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

import com.google.inject.Injector;
import com.google.inject.util.Providers;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

class CronTaskFactory {

  private final Injector injector;
  private final PrivilegedRunnableFactory runnableFactory;

  @Inject
  public CronTaskFactory(Injector injector, PrivilegedRunnableFactory runnableFactory) {
    this.injector = injector;
    this.runnableFactory = runnableFactory;
  }

  CronTask create(String expression, Runnable runnable) {
    return create(expression, runnable.getClass().getName(), Providers.of(runnable));
  }

  CronTask create(String expression, Class<? extends Runnable> runnable) {
    return create(expression, runnable.getName(), injector.getProvider(runnable));
  }

  private CronTask create(String expression, String name, Provider<? extends Runnable> runnableProvider) {
    Runnable runnable = runnableFactory.create(runnableProvider);
    return new CronTask(name, new CronExpression(expression), runnable);
  }
}

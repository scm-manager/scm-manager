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

package sonia.scm.update.repository;

import com.google.inject.Injector;
import sonia.scm.update.repository.MigrationStrategy.Instance;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MigrationStrategyMock {

  static Injector init() {
    Map<Class, Instance> mocks = new HashMap<>();
    Injector mock = mock(Injector.class);
    when(
      mock.getInstance(any(Class.class)))
      .thenAnswer(
        invocationOnMock -> mocks.computeIfAbsent(invocationOnMock.getArgument(0), key -> mock((Class<Instance>) key))
      );

    for (MigrationStrategy strategy : MigrationStrategy.values()) {
      MigrationStrategy.Instance strategyMock = mock(strategy.getImplementationClass());
      when(strategyMock.migrate(any(), any(), any())).thenReturn(of(Paths.get("")));
      lenient().when(mock.getInstance((Class<MigrationStrategy.Instance>) strategy.getImplementationClass())).thenReturn(strategyMock);
    }

    return mock;
  }
}

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

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
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CronTaskFactoryTest {

  @Mock
  private Injector injector;

  @Mock
  private PrivilegedRunnableFactory runnableFactory;

  @InjectMocks
  private CronTaskFactory taskFactory;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUpMocks() {
    when(runnableFactory.create(any(Provider.class))).thenAnswer(ic -> {
      Provider<? extends Runnable> r = ic.getArgument(0);
      return r.get();
    });
  }

  @Test
  void shouldCreateATaskWithNameFromRunnable() {
    CronTask task = taskFactory.create("30 * * * * ?", new One());
    assertThat(task.getName()).isEqualTo(One.class.getName());
  }

  @Test
  void shouldCreateATaskWithNameFromClass() {
    when(injector.getProvider(One.class)).thenReturn(Providers.of(new One()));

    CronTask task = taskFactory.create("30 * * * * ?", One.class);
    assertThat(task.getName()).isEqualTo(One.class.getName());
  }

  @Test
  void shouldCreateATaskWithCronExpression() {
    CronTask task = taskFactory.create("30 * * * * ?", new One());
    assertThat(task.getExpression().toString()).isEqualTo("30 * * * * ?");
  }

  public static class One implements Runnable {

    @Override
    public void run() {

    }
  }
}

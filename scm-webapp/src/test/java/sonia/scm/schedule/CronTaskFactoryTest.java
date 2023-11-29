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

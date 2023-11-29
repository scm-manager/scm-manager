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

package sonia.scm.work;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ThreadCountProviderTest {

  @Test
  void shouldUseTwoWorkersForOneCPU() {
    ThreadCountProvider provider = new ThreadCountProvider(() -> 1, 2);

    assertThat(provider.getAsInt()).isEqualTo(2);
  }

  @ParameterizedTest(name = "shouldUseFourWorkersFor{argumentsWithNames}CPU")
  @ValueSource(ints = {2, 4, 8, 16})
  void shouldUseFourWorkersForMoreThanOneCPU(int cpus) {
    ThreadCountProvider provider = new ThreadCountProvider(() -> cpus, 4);

    assertThat(provider.getAsInt()).isEqualTo(4);
  }

  @Nested
  class ConfigValueTests {

    @Test
    void shouldUseCountFromSystemProperty() {
      ThreadCountProvider provider = new ThreadCountProvider(6);
      assertThat(provider.getAsInt()).isEqualTo(6);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "100", "a", ""})
    void shouldUseDefaultForInvalidValue(String value) {
      ThreadCountProvider provider = new ThreadCountProvider(() -> 1, Integer.parseInt(value));
      assertThat(provider.getAsInt()).isEqualTo(2);
    }

  }

}

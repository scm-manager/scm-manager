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
    @ValueSource(strings = {"-1", "0", "100"})
    void shouldUseDefaultForInvalidValue(String value) {
      ThreadCountProvider provider = new ThreadCountProvider(() -> 1, Integer.parseInt(value));
      assertThat(provider.getAsInt()).isEqualTo(2);
    }

  }

}

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

package sonia.scm.api.v2.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

class HalEnricherContextTest {

  @Test
  void shouldCreateContextFromSingleObject() {
    HalEnricherContext context = HalEnricherContext.of("hello");
    assertThat(context.oneByType(String.class)).contains("hello");
  }

  @Test
  void shouldCreateContextFromMultipleObjects() {
    HalEnricherContext context = HalEnricherContext.of("hello", Integer.valueOf(42), Long.valueOf(21L));
    assertThat(context.oneByType(String.class)).contains("hello");
    assertThat(context.oneByType(Integer.class)).contains(42);
    assertThat(context.oneByType(Long.class)).contains(21L);
  }

  @Test
  void shouldReturnEmptyOptionalForUnknownTypes() {
    HalEnricherContext context = HalEnricherContext.of();
    assertThat(context.oneByType(String.class)).isNotPresent();
  }

  @Test
  void shouldReturnRequiredObject() {
    HalEnricherContext context = HalEnricherContext.of("hello");
    assertThat(context.oneRequireByType(String.class)).isEqualTo("hello");
  }

  @Test
  void shouldThrowAnNoSuchElementExceptionForUnknownTypes() {
    HalEnricherContext context = HalEnricherContext.of();
    assertThrows(NoSuchElementException.class, () -> context.oneRequireByType(String.class));
  }

}

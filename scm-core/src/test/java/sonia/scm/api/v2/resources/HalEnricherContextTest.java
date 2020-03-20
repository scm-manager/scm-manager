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

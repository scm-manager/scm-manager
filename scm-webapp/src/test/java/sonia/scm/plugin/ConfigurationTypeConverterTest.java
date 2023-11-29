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

package sonia.scm.plugin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTypeConverterTest {

  @Test
  void shouldConvertInt() {
    Object result = ConfigurationTypeConverter.convert("int", "42");

    assertThat(result)
      .isInstanceOf(Integer.class)
      .isEqualTo(42);
  }

  @Test
  void shouldConvertInteger() {
    Object result = ConfigurationTypeConverter.convert("java.lang.Integer", "42");

    assertThat(result)
      .isInstanceOf(Integer.class)
      .isEqualTo(42);
  }


  @Test
  void shouldConvertString() {
    Object result = ConfigurationTypeConverter.convert("java.lang.String", "hello");

    assertThat(result)
      .isInstanceOf(String.class)
      .isEqualTo("hello");
  }

  @Test
  void shouldConvertBoolean() {
    Object result = ConfigurationTypeConverter.convert("boolean", "true");

    assertThat(result)
      .isInstanceOf(Boolean.class)
      .isEqualTo(true);
  }

  @Test
  void shouldConvertLong() {
    Object result = ConfigurationTypeConverter.convert("long", "42");

    assertThat(result)
      .isInstanceOf(Long.class)
      .isEqualTo(42L);
  }

  @Test
  void shouldConvertFloat() {
    Object result = ConfigurationTypeConverter.convert("float", "42.0");

    assertThat(result)
      .isInstanceOf(Float.class)
      .isEqualTo(42.0f);
  }

}

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

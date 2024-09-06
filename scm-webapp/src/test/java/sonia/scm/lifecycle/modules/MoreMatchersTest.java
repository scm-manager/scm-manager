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

package sonia.scm.lifecycle.modules;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import jakarta.inject.Singleton;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;

class MoreMatchersTest {

  @Test
  void shouldMatchSubTypes() {
    Matcher<TypeLiteral> matcher = MoreMatchers.isSubtypeOf(Serializable.class);
    assertBoolean(matcher, One.class).isTrue();
    assertBoolean(matcher, Two.class).isFalse();
  }

  @Test
  void shouldMatchIfAnnotated() {
    Matcher<TypeLiteral> matcher = MoreMatchers.isAnnotatedWith(Singleton.class);
    assertBoolean(matcher, One.class).isFalse();
    assertBoolean(matcher, Two.class).isTrue();
  }

  private AbstractBooleanAssert<?> assertBoolean(Matcher<TypeLiteral> matcher, Class<?> clazz) {
    return assertThat(matcher.matches(TypeLiteral.get(clazz)));
  }

  public static class One implements Serializable {
  }

  @Singleton
  public static class Two {
  }

}

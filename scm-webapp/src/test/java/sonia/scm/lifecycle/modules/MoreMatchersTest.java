package sonia.scm.lifecycle.modules;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.jupiter.api.Test;

import javax.inject.Singleton;
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

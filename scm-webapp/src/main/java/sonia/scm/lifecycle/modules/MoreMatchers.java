package sonia.scm.lifecycle.modules;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

import java.lang.annotation.Annotation;

/**
 * Helper methods for Guice matchers, which are not already provided in {@link com.google.inject.matcher.Matchers}.
 */
@SuppressWarnings("unchecked")
final class MoreMatchers {

  private MoreMatchers() {}

  /**
   * Returns a matcher which matches TypeListerals which are annotated with the given annotation.
   *
   * @param annotation annotation to match
   *
   * @return annotation matcher
   */
  static Matcher<TypeLiteral> isAnnotatedWith(final Class<? extends Annotation> annotation) {
    return new AbstractMatcher<TypeLiteral>() {
      @Override
      public boolean matches(TypeLiteral type) {
        return type.getRawType().isAnnotationPresent(annotation);
      }
    };
  }

  /**
   * Returns a matcher which maches TypeLiterals which are sub types of the given class.
   *
   * @param supertype sub type to match
   *
   * @return sub type matcher
   */
  static Matcher<TypeLiteral> isSubtypeOf(final Class supertype) {
    return isSubtypeOf(TypeLiteral.get(supertype));
  }

  private static Matcher<TypeLiteral> isSubtypeOf(final TypeLiteral supertype) {
    return new AbstractMatcher<TypeLiteral>() {
      @Override
      public boolean matches(TypeLiteral type) {
        return typeIsSubtypeOf(type, supertype);
      }
    };
  }

  private static boolean typeIsSubtypeOf(TypeLiteral subtype, TypeLiteral supertype) {
    // First check that raw types are compatible
    // Then check that generic types are compatible! HOW????
    return (subtype.equals(supertype)
      || (supertype.getRawType().isAssignableFrom(subtype.getRawType())
      && supertype.equals(subtype.getSupertype(supertype.getRawType()))));
  }
}

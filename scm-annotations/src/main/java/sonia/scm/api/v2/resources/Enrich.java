package sonia.scm.api.v2.resources;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify the source of an enricher.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Enrich {

  /**
   * Source mapping class.
   *
   * @return source mapping class
   */
  Class<?> value();
}

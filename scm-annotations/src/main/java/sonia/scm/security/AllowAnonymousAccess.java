package sonia.scm.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to mark REST resource methods that may be accessed <b>without</b> authentication.
 * To mark all methods of a complete class you can annotate the class instead.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowAnonymousAccess {
}

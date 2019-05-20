package sonia.scm.api.v2.resources;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = EitherRoleOrVerbsValidator.class)
@Documented
public @interface EitherRoleOrVerbs {

  String message() default "permission must either have a role or a not empty set of verbs";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}

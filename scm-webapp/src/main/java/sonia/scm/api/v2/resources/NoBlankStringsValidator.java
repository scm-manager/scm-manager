package sonia.scm.api.v2.resources;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

public class NoBlankStringsValidator implements ConstraintValidator<NoBlankStrings, Collection> {

  @Override
  public void initialize(NoBlankStrings constraintAnnotation) {
  }

  @Override
  public boolean isValid(Collection object, ConstraintValidatorContext constraintContext) {
    if ( object == null || object.isEmpty()) {
      return true;
    }
    return object.stream()
      .map(x -> x.toString())
      .map(s -> ((String) s).trim())
      .noneMatch(s -> ((String) s).isEmpty());
  }
}

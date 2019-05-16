package sonia.scm.api.v2.resources;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EitherRoleOrVerbsValidator implements ConstraintValidator<EitherRoleOrVerbs, RepositoryPermissionDto> {

  private static final Logger LOG = LoggerFactory.getLogger(EitherRoleOrVerbsValidator.class);

  @Override
  public void initialize(EitherRoleOrVerbs constraintAnnotation) {
  }

  @Override
  public boolean isValid(RepositoryPermissionDto object, ConstraintValidatorContext constraintContext) {
    if (Strings.isNullOrEmpty(object.getRole())) {
      boolean result = object.getVerbs() != null && !object.getVerbs().isEmpty();
      LOG.trace("Validation result for permission with empty or no role: {}", result);
      return result;
    } else {
      boolean result = object.getVerbs() == null || object.getVerbs().isEmpty();
      LOG.trace("Validation result for permission with non empty role: {}", result);
      return result;
    }
  }
}

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

package sonia.scm.api.v2.resources;

import com.google.common.base.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

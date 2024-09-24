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

package sonia.scm.repository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import sonia.scm.util.ValidationUtil;

public class RepositoryNameConstrainValidator implements ConstraintValidator<RepositoryName, String> {

  private RepositoryName.Namespace namespace;

  @Override
  public void initialize(RepositoryName constraintAnnotation) {
    namespace = constraintAnnotation.namespace();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    String[] parts = value.split("/");
    if (namespace == RepositoryName.Namespace.REQUIRED) {
      if (parts.length == 2) {
        return ValidationUtil.isRepositoryNameValid(parts[1]);
      }
      return false;
    } else if (namespace == RepositoryName.Namespace.OPTIONAL) {
      if (parts.length == 2) {
        return ValidationUtil.isRepositoryNameValid(parts[1]);
      } else if (parts.length == 1) {
        return ValidationUtil.isRepositoryNameValid(parts[0]);
      } else {
        return false;
      }
    }
    return ValidationUtil.isRepositoryNameValid(value);
  }
}

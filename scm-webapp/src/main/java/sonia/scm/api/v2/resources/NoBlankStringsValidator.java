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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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

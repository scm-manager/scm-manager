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

package sonia.scm.validation;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

public class DefaultValidatorProvider implements Provider<Validator> {

  private final ConstraintValidatorFactory constraintValidatorFactory;

  @Inject
  public DefaultValidatorProvider(ConstraintValidatorFactory constraintValidatorFactory) {
    this.constraintValidatorFactory = constraintValidatorFactory;
  }

  @Override
  public Validator get() {
    return Validation.buildDefaultValidatorFactory()
      .usingContext()
      .constraintValidatorFactory(constraintValidatorFactory)
      .getValidator();
  }
}

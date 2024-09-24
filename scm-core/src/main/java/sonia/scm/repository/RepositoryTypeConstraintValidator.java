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

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import sonia.scm.Type;

import java.util.stream.Collectors;

/**
 * Validator for {@link RepositoryTypeConstraint}.
 *
 * @since 2.33.0
 */
public class RepositoryTypeConstraintValidator implements ConstraintValidator<RepositoryTypeConstraint, String> {

  private final RepositoryManager repositoryManager;

  @Inject
  public RepositoryTypeConstraintValidator(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  public RepositoryManager getRepositoryManager() {
    return repositoryManager;
  }

  @Override
  public boolean isValid(String type, ConstraintValidatorContext context) {
    if (!isSupportedType(type)) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(createMessage(context)).addConstraintViolation();
      return false;
    }
    return true;
  }

  private boolean isSupportedType(String type) {
    return repositoryManager.getConfiguredTypes()
      .stream().anyMatch(t -> t.getName().equals(type));
  }

  private String createMessage(ConstraintValidatorContext context) {
    String message = context.getDefaultConstraintMessageTemplate();
    return message + " " + commaSeparatedTypes();
  }

  private String commaSeparatedTypes() {
    return repositoryManager.getConfiguredTypes()
      .stream()
      .map(Type::getName)
      .collect(Collectors.joining(", "));
  }
}

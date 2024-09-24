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
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import sonia.scm.repository.RepositoryManager;

public class RepositoryTypeConstraintValidator implements ConstraintValidator<RepositoryType, String> {

  private final RepositoryManager repositoryManager;

  @Inject
  public RepositoryTypeConstraintValidator(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  public RepositoryManager getRepositoryManager() {
    return repositoryManager;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return repositoryManager.getConfiguredTypes()
      .stream().anyMatch(t -> t.getName().equalsIgnoreCase(value));
  }
}

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

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryManager;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ValidationModuleTest {

  @Nested
  class SimpleInjectionTests {

    private Injector injector;

    @BeforeEach
    void setUpInjector() {
      injector = Guice.createInjector(new ValidationModule());
    }

    @Test
    void shouldInjectValidator() {
      WithValidator instance = injector.getInstance(WithValidator.class);
      assertThat(instance.validator).isNotNull();
    }

    @Test
    void shouldInjectConstraintValidatorFactory() {
      WithConstraintValidatorFactory instance = injector.getInstance(WithConstraintValidatorFactory.class);
      assertThat(instance.constraintValidatorFactory).isInstanceOf(GuiceConstraintValidatorFactory.class);
    }

  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  class RealWorldTests {

    @Mock
    private RepositoryManager repositoryManager;

    @Test
    void shouldValidateRepositoryTypes() {
      when(repositoryManager.getConfiguredTypes()).thenReturn(ImmutableList.of(
        new sonia.scm.repository.RepositoryType("git", "Git", Collections.emptySet()),
        new sonia.scm.repository.RepositoryType("hg", "Mercurial", Collections.emptySet())
      ));

      Injector injector = Guice.createInjector(new ValidationModule(), new RepositoryManagerModule(repositoryManager));

      Validator validator = injector.getInstance(Validator.class);

      Set<ConstraintViolation<Repository>> violations = validator.validate(new Repository("svn"));
      assertThat(violations).isNotEmpty();

      violations = validator.validate(new Repository("git"));
      assertThat(violations).isEmpty();
    }

  }


  public static class WithValidator {

    private final Validator validator;

    @Inject
    public WithValidator(Validator validator) {
      this.validator = validator;
    }
  }

  public static class WithConstraintValidatorFactory {

    private final ConstraintValidatorFactory constraintValidatorFactory;

    @Inject
    public WithConstraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
      this.constraintValidatorFactory = constraintValidatorFactory;
    }
  }


  @AllArgsConstructor
  public static class Repository {

    @RepositoryType
    private String type;

  }

}

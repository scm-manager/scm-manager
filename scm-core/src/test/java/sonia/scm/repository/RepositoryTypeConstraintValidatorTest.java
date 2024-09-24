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
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.DefaultLocale;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTypeConstraintValidatorTest {

  @Mock
  private RepositoryManager repositoryManager;

  @Test
  @DefaultLocale("en")
  void shouldFailValidation() {
    mockRepositoryTypes("git", "hg");

    Validator validator = validator();
    Set<ConstraintViolation<Repo>> violations = validator.validate(new Repo("svn"));
    assertThat(violations).hasSize(1).allSatisfy(
      violation -> assertThat(violation.getMessage()).contains("Invalid repository type")
    );
  }

  @Test
  @DefaultLocale("de")
  void shouldFailValidationWithGermanMessage() {
    mockRepositoryTypes("svn", "hg");

    Validator validator = validator();
    Set<ConstraintViolation<Repo>> violations = validator.validate(new Repo("git"));
    assertThat(violations).hasSize(1).allSatisfy(
      violation -> assertThat(violation.getMessage()).contains("Ungültiger Repository-Typ")
    );
  }

  @Test
  @DefaultLocale("en")
  void shouldPassValidation() {
    mockRepositoryTypes("svn", "git", "hg");

    Validator validator = validator();
    Set<ConstraintViolation<Repo>> violations = validator.validate(new Repo("hg"));
    assertThat(violations).isEmpty();
  }

  @Test
  @DefaultLocale("en")
  void shouldAddAvailableTypesToMessage() {
    mockRepositoryTypes("git", "hg", "svn");

    Validator validator = validator();
    Set<ConstraintViolation<Repo>> violations = validator.validate(new Repo("unknown"));
    assertThat(violations).hasSize(1).allSatisfy(
      violation -> assertThat(violation.getMessage()).contains("git, hg, svn")
    );
  }

  private Validator validator() {
    return Validation.buildDefaultValidatorFactory()
      .usingContext()
      .constraintValidatorFactory(new TestingConstraintValidatorFactory(repositoryManager))
      .getValidator();
  }

  private void mockRepositoryTypes(String... types) {
    List<RepositoryType> repositoryTypes = Arrays.stream(types)
      .map(t -> new RepositoryType(t, t.toUpperCase(Locale.ENGLISH), Collections.emptySet()))
      .collect(Collectors.toList());

    when(repositoryManager.getConfiguredTypes()).thenReturn(repositoryTypes);
  }

  public static class Repo {

    @RepositoryTypeConstraint
    private final String type;

    public Repo(String type) {
      this.type = type;
    }
  }

  public static class TestingConstraintValidatorFactory implements ConstraintValidatorFactory {

    private final RepositoryManager repositoryManager;

    public TestingConstraintValidatorFactory(RepositoryManager repositoryManager) {
      this.repositoryManager = repositoryManager;
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
      try {
        return key.getConstructor(RepositoryManager.class).newInstance(repositoryManager);
      } catch (Exception ex) {
        try {
          return key.getConstructor().newInstance();
        } catch (Exception e) {
          throw new IllegalStateException("Failed to create constraint", e);
        }
      }
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }
  }

}

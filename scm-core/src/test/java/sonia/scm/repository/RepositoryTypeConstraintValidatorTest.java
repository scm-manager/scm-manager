/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

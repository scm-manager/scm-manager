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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.RepositoryName.Namespace.OPTIONAL;
import static sonia.scm.repository.RepositoryName.Namespace.REQUIRED;

class RepositoryNameConstrainValidatorTest {

  @Nested
  class WithoutNamespace {

    @Test
    void shouldPassValidation() {
      assertThat(validate(new NamespaceNone("scm-manager"))).isEmpty();
    }

    @Test
    @DefaultLocale("en")
    void shouldFailValidation() {
      assertThat(validate(new NamespaceNone("scm\\manager"))).hasSize(1).allSatisfy(
        violation -> assertThat(violation.getMessage()).isEqualTo("Invalid repository name")
      );
    }

    @Test
    @DefaultLocale("de")
    void shouldFailValidationWithGermanMessage() {
      assertThat(validate(new NamespaceNone("scm\\manager"))).hasSize(1).allSatisfy(
        violation -> assertThat(violation.getMessage()).isEqualTo("Ungültiger Repository Name")
      );
    }

    @Test
    @DefaultLocale("en")
    void shouldFailWithSlashAndDisabledWithNamespaceOption() {
      assertThat(validate(new NamespaceNone("scm/manager"))).isNotEmpty();
    }

  }

  @Nested
  class WithRequiredNamespace {

    @Test
    void shouldPassValidation() {
      assertThat(validate(new RequiredNamespace("scm/manager"))).isEmpty();
    }

    @Test
    void shouldFailWithMoreThanOneSlash() {
      assertThat(validate(new RequiredNamespace("scm/mana/ger"))).isNotEmpty();
    }

    @Test
    void shouldFailWithOutNamespace() {
      assertThat(validate(new RequiredNamespace("scm-manager"))).isNotEmpty();
    }

    @Test
    void shouldFailWithInvalidName() {
      assertThat(validate(new RequiredNamespace("scm/ma\\nager"))).isNotEmpty();
    }

  }

  @Nested
  class WithOptionalNamespace {

    @Test
    void shouldPassValidationWithoutNamespace() {
      assertThat(validate(new OptionalNamespace("scm-manager"))).isEmpty();
    }

    @Test
    void shouldPassValidationWithNamespace() {
      assertThat(validate(new OptionalNamespace("scm/manager"))).isEmpty();
    }

    @Test
    void shouldFailWithMoreThanOneSlash() {
      assertThat(validate(new OptionalNamespace("scm/mana/ger"))).isNotEmpty();
    }

    @Test
    void shouldFailWithInvalidName() {
      assertThat(validate(new OptionalNamespace("scm/ma\\nager"))).isNotEmpty();
    }

  }

  private <T> Set<ConstraintViolation<T>> validate(T object) {
    return validator().validate(object);
  }

  private Validator validator() {
    return Validation.buildDefaultValidatorFactory().getValidator();
  }

  public static class NamespaceNone {

    @RepositoryName
    private final String name;

    public NamespaceNone(String name) {
      this.name = name;
    }
  }

  public static class RequiredNamespace {

    @RepositoryName(namespace = REQUIRED)
    private final String name;

    public RequiredNamespace(String name) {
      this.name = name;
    }
  }

  public static class OptionalNamespace {

    @RepositoryName(namespace = OPTIONAL)
    private final String name;

    public OptionalNamespace(String name) {
      this.name = name;
    }
  }

}

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

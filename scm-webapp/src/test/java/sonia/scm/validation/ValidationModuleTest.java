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

package sonia.scm.validation;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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

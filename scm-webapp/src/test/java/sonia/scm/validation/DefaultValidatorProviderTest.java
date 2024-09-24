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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultValidatorProviderTest {

  @Test
  void shouldCreateValidatorWithConstraintValidatorFactory() {
    TestingConstraintValidatorFactory constraintValidatorFactory = new TestingConstraintValidatorFactory();
    DefaultValidatorProvider provider = new DefaultValidatorProvider(constraintValidatorFactory);
    Validator validator = provider.get();
    validator.validate(new Sample("one"));

    assertThat(constraintValidatorFactory.counter).isOne();
  }

  private static class TestingConstraintValidatorFactory implements ConstraintValidatorFactory {

    private int counter = 0;

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
      counter++;
      try {
        return key.getConstructor().newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }
  }

  private static class Sample {

    @Size(max = 20)
    private final String value;

    public Sample(String value) {
      this.value = value;
    }
  }

}

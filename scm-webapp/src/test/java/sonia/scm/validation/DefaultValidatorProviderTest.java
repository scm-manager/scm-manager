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

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validator;
import javax.validation.constraints.Size;

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

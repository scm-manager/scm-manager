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

package sonia.scm.web.api;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DtoValidatorTest {

  @Test
  void shouldValidateInvalidBean() {
    assertThrows(
      ValidationException.class,
      () -> DtoValidator.validate(new AnyQuestion(43))
    );
  }

  @Test
  void shouldValidateValidBean() {
    DtoValidator.validate(new AnyQuestion(42));
  }

  static class AnyQuestion {
    @Min(42)
    @Max(42)
    int answer;

    public AnyQuestion(int answer) {
      this.answer = answer;
    }
  }
}

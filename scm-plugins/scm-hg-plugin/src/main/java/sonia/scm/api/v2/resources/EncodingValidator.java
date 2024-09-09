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

package sonia.scm.api.v2.resources;

import com.google.common.base.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class EncodingValidator implements ConstraintValidator<Encoding, String> {

  @Override
  public void initialize(Encoding constraintAnnotation) {
    // do nothing
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (Strings.isNullOrEmpty(value)) {
      return true;
    }
    try {
      Charset.forName(value);
      return true;
    } catch (UnsupportedCharsetException ex) {
      return false;
    }
  }
}

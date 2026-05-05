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
import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.ParamConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Singleton
public final class LocalDateParamConverter implements ParamConverter<LocalDate> {

  @Override
  public LocalDate fromString(String value) {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }

    try {
      return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException e) {
      throw new MalformedLocalDateException(value);
    }
  }

  @Override
  public String toString(LocalDate value) {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(value);
  }
}

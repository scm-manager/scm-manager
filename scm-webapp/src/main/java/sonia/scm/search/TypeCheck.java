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

package sonia.scm.search;

import java.time.Instant;

public final class TypeCheck {

  private TypeCheck() {
  }

  public static boolean isLong(Class<?> type) {
    return type == Long.TYPE || type == Long.class;
  }

  public static boolean isInteger(Class<?> type) {
    return type == Integer.TYPE || type == Integer.class;
  }

  public static boolean isBoolean(Class<?> type) {
    return type == Boolean.TYPE || type == Boolean.class;
  }

  public static boolean isInstant(Class<?> type) {
    return type == Instant.class;
  }

  public static boolean isString(Class<?> type) {
    return type == String.class;
  }

  public static boolean isNumber(Class<?> type) { return isLong(type) || isInteger(type); }
}

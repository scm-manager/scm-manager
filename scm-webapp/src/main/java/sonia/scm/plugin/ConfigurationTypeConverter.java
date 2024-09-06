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

package sonia.scm.plugin;

class ConfigurationTypeConverter {

  private ConfigurationTypeConverter() {
  }

  static Object convert(String type, String value) {
    return switch (type) {
      case "java.lang.String" -> value;
      case "int", "java.lang.Integer" -> Integer.parseInt(value);
      case "boolean", "java.lang.Boolean" -> Boolean.parseBoolean(value);
      case "long", "java.lang.Long" -> Long.parseLong(value);
      case "double", "java.lang.Double" -> Double.parseDouble(value);
      case "float", "java.lang.Float" -> Float.parseFloat(value);
      default -> throw new IllegalArgumentException("Unknown type: " + type);
    };
  }
}

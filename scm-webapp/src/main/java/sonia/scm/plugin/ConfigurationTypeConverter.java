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

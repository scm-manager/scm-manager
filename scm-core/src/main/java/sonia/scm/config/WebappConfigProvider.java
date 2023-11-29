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

package sonia.scm.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WebappConfigProvider {

  private WebappConfigProvider() {}

  private static Map<String, String> configBindings = new HashMap<>();

  public static void setConfigBindings(Map<String, String> newBindings) {
    configBindings = newBindings;
  }

  public static Optional<String> resolveAsString(String key) {
    return resolveConfig(key);
  }

  public static Optional<Boolean> resolveAsBoolean(String key) {
    return resolveConfig(key).map(Boolean::parseBoolean);
  }

  public static Optional<Integer> resolveAsInteger(String key) {
    return resolveConfig(key).map(Integer::parseInt);
  }

  public static Optional<Long> resolveAsLong(String key) {
    return resolveConfig(key).map(Long::parseLong);
  }

  private static Optional<String> resolveConfig(String key) {
    return Optional.ofNullable(configBindings.get(key));
  }
}

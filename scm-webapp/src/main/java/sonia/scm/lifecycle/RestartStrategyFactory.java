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
package sonia.scm.lifecycle;

import com.google.common.base.Strings;
import sonia.scm.PlatformType;
import sonia.scm.util.SystemUtil;

final class RestartStrategyFactory {

  /**
   * System property to load a specific restart strategy.
   */
  static final String PROPERTY_STRATEGY = "sonia.scm.lifecycle.restart-strategy";

  /**
   * No restart supported.
   */
  static final  String STRATEGY_NONE = "none";

  private RestartStrategyFactory() {
  }

  /**
   * Returns the configured strategy or {@code null} if restart is not supported.
   *
   * @param webAppClassLoader root webapp classloader
   * @return configured strategy or {@code null}
   */
  static RestartStrategy create(ClassLoader webAppClassLoader) {
    String property = System.getProperty(PROPERTY_STRATEGY);
    if (Strings.isNullOrEmpty(property)) {
      return forPlatform();
    }
    return fromProperty(webAppClassLoader, property);
  }

  private static RestartStrategy fromProperty(ClassLoader webAppClassLoader, String property) {
    if (STRATEGY_NONE.equalsIgnoreCase(property)) {
      return null;
    } else if (ExitRestartStrategy.NAME.equalsIgnoreCase(property)) {
      return new ExitRestartStrategy();
    } else if (InjectionContextRestartStrategy.NAME.equalsIgnoreCase(property)) {
      return new InjectionContextRestartStrategy(webAppClassLoader);
    } else {
      return fromClassName(property);
    }
  }

  private static RestartStrategy fromClassName(String property) {
    try {
      Class<? extends RestartStrategy> rsClass = Class.forName(property).asSubclass(RestartStrategy.class);
      return rsClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new RestartNotSupportedException("failed to create restart strategy from property", e);
    }
  }

  private static RestartStrategy forPlatform() {
    // we do not use SystemUtil here, to allow testing
    String osName = System.getProperty(SystemUtil.PROPERTY_OSNAME);
    PlatformType platform = PlatformType.createPlatformType(osName);
    if (platform.isPosix()) {
      return new PosixRestartStrategy();
    }
    return null;
  }
}

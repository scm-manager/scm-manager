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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import sonia.scm.Platform;
import sonia.scm.util.SystemUtil;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Properties;

final class RestartStrategyFactory {

  /**
   * System property to load a specific restart strategy.
   */
  static final String PROPERTY_STRATEGY = "sonia.scm.lifecycle.restart-strategy";

  /**
   * No restart supported.
   */
  static final  String STRATEGY_NONE = "none";

  private final Platform platform;
  private final Map<String, String> environment;
  private final Properties systemProperties;

  @VisibleForTesting
  RestartStrategyFactory(Platform platform, Map<String, String> environment, Properties systemProperties) {
    this.platform = platform;
    this.environment = environment;
    this.systemProperties = systemProperties;
  }

  /**
   * Returns the configured strategy or {@code null} if restart is not supported.
   *
   * @param webAppClassLoader root webapp classloader
   * @return configured strategy or {@code null}
   */
  static RestartStrategy create(ClassLoader webAppClassLoader) {
    RestartStrategyFactory factory = new RestartStrategyFactory(
      SystemUtil.getPlatform(),
      System.getenv(),
      System.getProperties()
    );
    return factory.fromClassLoader(webAppClassLoader);
  }

  @VisibleForTesting
  RestartStrategy fromClassLoader(ClassLoader webAppClassLoader) {
    String property = systemProperties.getProperty(PROPERTY_STRATEGY);
    if (Strings.isNullOrEmpty(property)) {
      return forPlatform();
    }
    return fromProperty(webAppClassLoader, property);
  }

  private RestartStrategy fromProperty(ClassLoader webAppClassLoader, String property) {
    if (STRATEGY_NONE.equalsIgnoreCase(property)) {
      return null;
    } else if (ExitRestartStrategy.NAME.equalsIgnoreCase(property)) {
      return new ExitRestartStrategy();
    } else {
      return fromClassName(property, webAppClassLoader);
    }
  }

  private RestartStrategy fromClassName(String className, ClassLoader classLoader) {
    try {
      Class<? extends RestartStrategy> rsClass = Class.forName(className).asSubclass(RestartStrategy.class);
      return createInstance(rsClass, classLoader);
    } catch (Exception e) {
      throw new RestartNotSupportedException("failed to create restart strategy from property", e);
    }
  }

  private RestartStrategy createInstance(Class<? extends RestartStrategy> rsClass, ClassLoader classLoader) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
    try {
      Constructor<? extends RestartStrategy> constructor = rsClass.getConstructor(ClassLoader.class);
      return constructor.newInstance(classLoader);
    } catch (NoSuchMethodException ex) {
      return rsClass.getConstructor().newInstance();
    }
  }

  private RestartStrategy forPlatform() {
    if (platform.isPosix()) {
      return new PosixRestartStrategy();
    } else if (platform.isWindows() && WinSWRestartStrategy.isSupported(environment)) {
      return new WinSWRestartStrategy();
    }
    return null;
  }
}

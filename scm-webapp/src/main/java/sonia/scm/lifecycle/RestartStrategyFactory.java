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

package sonia.scm.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import sonia.scm.Platform;
import sonia.scm.config.WebappConfigProvider;
import sonia.scm.util.SystemUtil;

import java.lang.reflect.Constructor;
import java.util.Map;

final class RestartStrategyFactory {

  /**
   * System property to load a specific restart strategy.
   */
  static final String RESTART_STRATEGY = "restart.strategy";

  /**
   * No restart supported.
   */
  static final  String STRATEGY_NONE = "none";

  private final Platform platform;
  private final Map<String, String> environment;

  @VisibleForTesting
  RestartStrategyFactory(Platform platform, Map<String, String> environment) {
    this.platform = platform;
    this.environment = environment;
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
      System.getenv()
    );
    return factory.fromClassLoader(webAppClassLoader);
  }

  @VisibleForTesting
  RestartStrategy fromClassLoader(ClassLoader webAppClassLoader) {
    String strategy = WebappConfigProvider.resolveAsString(RESTART_STRATEGY).orElse(null);
    if (Strings.isNullOrEmpty(strategy)) {
      return forPlatform();
    }
    return forStrategy(webAppClassLoader, strategy);
  }

  private RestartStrategy forStrategy(ClassLoader webAppClassLoader, String strategy) {
    if (STRATEGY_NONE.equalsIgnoreCase(strategy)) {
      return null;
    } else if (ExitRestartStrategy.NAME.equalsIgnoreCase(strategy)) {
      return new ExitRestartStrategy();
    } else {
      return fromClassName(strategy, webAppClassLoader);
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

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Strategy for restarting SCM-Manager. Implementations must either have a default constructor or one taking the
 * class loader for the current context as a single argument.
 */
public abstract class RestartStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(RestartStrategy.class);

  interface InternalInjectionContext extends InjectionContext {
    /**
     * Destroys the injection context.
     */
    void destroy();
  }

  /**
   * Context for Injection in SCM-Manager.
   */
  public interface InjectionContext {
    /**
     * Initialize the injection context.
     */
    void initialize();
  }

  /**
   * Restart SCM-Manager by first calling {@link #prepareRestart(InjectionContext)}, destroying the
   * current context, and finally calling {@link #executeRestart(InjectionContext)}.
   *
   * @param context injection context
   */
  public final void restart(InternalInjectionContext context) {
    prepareRestart(context);
    LOG.warn("destroy injection context");
    context.destroy();
    executeRestart(context);
  }

  /**
   * Prepare the restart of SCM-Manager. Here you can check whether restart is possible and,
   * if necessary, throw a {@link RestartNotSupportedException} to abort the restart.
   *
   * @param context injection context
   */
  protected void prepareRestart(InjectionContext context) {
  }

  /**
   * Actually restart SCM-Manager.
   *
   * @param context injection context
   */
  protected abstract void executeRestart(InjectionContext context);

  /**
   * Returns the configured strategy or empty if restart is not supported by the underlying platform.
   *
   * @param webAppClassLoader root webapp classloader
   * @return configured strategy or empty optional
   */
  static Optional<RestartStrategy> get(ClassLoader webAppClassLoader) {
    return Optional.ofNullable(RestartStrategyFactory.create(webAppClassLoader));
  }

}

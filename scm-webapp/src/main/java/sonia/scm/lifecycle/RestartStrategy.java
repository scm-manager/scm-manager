package sonia.scm.lifecycle;

import java.util.Optional;

/**
 * Strategy for restarting SCM-Manager.
 */
public interface RestartStrategy {

  /**
   * Context for Injection in SCM-Manager.
   */
  interface InjectionContext {
    /**
     * Initialize the injection context.
     */
    void initialize();

    /**
     * Destroys the injection context.
     */
    void destroy();
  }

  /**
   * Restart SCM-Manager.
   *
   * @param context injection context
   */
  void restart(InjectionContext context);

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

package sonia.scm.lifecycle;

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
   * @param context injection context
   */
  void restart(InjectionContext context);

  /**
   * Returns the configured strategy.
   *
   * @return configured strategy
   */
  static RestartStrategy get(ClassLoader webAppClassLoader) {
    return new InjectionContextRestartStrategy(webAppClassLoader);
  }

}

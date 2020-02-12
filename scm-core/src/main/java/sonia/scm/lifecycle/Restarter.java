package sonia.scm.lifecycle;

/**
 * {@link Restarter} is able to restart scm-manager.
 *
 * @since 2.0.0
 */
public interface Restarter {

  /**
   * Return {@code true} if restarting scm-manager is supported.
   *
   * @return {@code true} if restart is supported
   */
  boolean isSupported();

  /**
   * Issues a restart. The method will fire a {@link RestartEvent} to notify the system about the upcoming restart.
   * If restarting is not supported by the underlying platform a {@link RestartNotSupportedException} is thrown.
   *
   * @param cause cause of the restart. This should be the class which calls this method.
   * @param reason reason for the required restart.
   */
  void restart(Class<?> cause, String reason);
}

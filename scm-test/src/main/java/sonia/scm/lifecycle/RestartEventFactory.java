package sonia.scm.lifecycle;

/**
 * Creates restart events for testing.
 * This is required, because the constructor of {@link RestartEvent} is package private.
 */
public final class RestartEventFactory {

  private RestartEventFactory(){}

  public static RestartEvent create(Class<?> cause, String reason) {
    return new RestartEvent(cause, reason);
  }

}

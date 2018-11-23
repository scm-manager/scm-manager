package sonia.scm.repository;

import java.util.function.Consumer;

public class CloseableWrapper<C> implements AutoCloseable {

  private final C wrapped;
  private final Consumer<C> cleanup;

  public CloseableWrapper(C wrapped, Consumer<C> cleanup) {
    this.wrapped = wrapped;
    this.cleanup = cleanup;
  }

  public C get() { return wrapped; }

  @Override
  public void close() {
    try {
      cleanup.accept(wrapped);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}

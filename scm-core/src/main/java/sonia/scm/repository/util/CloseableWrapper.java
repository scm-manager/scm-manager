package sonia.scm.repository.util;

import java.util.function.Consumer;

public class CloseableWrapper<T extends AutoCloseable> implements AutoCloseable {

  private final T wrapped;
  private final Consumer<T> cleanup;

  public CloseableWrapper(T wrapped, Consumer<T> cleanup) {
    this.wrapped = wrapped;
    this.cleanup = cleanup;
  }

  public T get() { return wrapped; }

  @Override
  public void close() {
    try {
      cleanup.accept(wrapped);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}

package sonia.scm;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Guice module which captures all classes which are implementing the {@link Closeable}. These classes can be later
 * closed, by injecting the {@link CloseableModule} and calling {@link #closeAll()}.
 *
 * @author Sebastian Sdorra
 */
public final class CloseableModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(CloseableModule.class);

  private final Deque<WeakReference<Closeable>> closeables = new ConcurrentLinkedDeque<>();

  @Override
  protected void configure() {
    bindListener(MoreMatchers.isSubtypeOf(Closeable.class), new TypeListener() {
      @Override
      public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        encounter.register((InjectionListener<I>) instance -> {
          LOG.debug("register closable {}", instance.getClass());
          Closeable closeable = (Closeable) instance;
          closeables.push(new WeakReference<>(closeable));
        });
      }
    });

    bind(CloseableModule.class).toInstance(this);
  }

  /**
   * Closes all captured instances.
   */
  public void closeAll() {
    LOG.debug("close all registered closeables");
    WeakReference<Closeable> reference = closeables.poll();
    while (reference != null) {
      Closeable closeable = reference.get();
      close(closeable);
      reference = closeables.poll();
    }
  }

  private void close(Closeable closeable) {
    if (closeable != null) {
      LOG.trace("close closeable instance of {}", closeable);
      IOUtil.close(closeable);
    }
  }

}

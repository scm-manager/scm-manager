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
    
package sonia.scm.lifecycle.modules;

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

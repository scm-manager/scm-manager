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


import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;

import java.util.Set;

/**
 * Guice module which captures all classes which are annotated with {@link EagerSingleton}. These classes can be later
 * initialized.
 *
 */
public class EagerSingletonModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(EagerSingletonModule.class);

  private final Set<Class<?>> eagerSingletons = Sets.newHashSet();

  /**
   * Initialize all captured classes.
   *
   * @param injector injector for initialization
   */
  void initialize(Injector injector) {
    for (Class<?> clazz : eagerSingletons) {
      LOG.info("initialize eager singleton {}", clazz.getName());
      injector.getInstance(clazz);
    }
  }

  @Override
  protected void configure() {
    bindListener(MoreMatchers.isAnnotatedWith(EagerSingleton.class), new TypeListener() {
      @Override
      public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        Class<? super I> rawType = type.getRawType();
        LOG.trace("register eager singleton {}", rawType);
        eagerSingletons.add(rawType);
      }
    });

    bind(EagerSingletonModule.class).toInstance(this);
  }

}

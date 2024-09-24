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

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

package sonia.scm.store;

import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import sonia.scm.util.AssertUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class StoreInvocationMicrometerWrapper implements InvocationHandler {

  private final Object store;
  private final MeterRegistry meterRegistry;
  private final Consumer<Timer.Builder> timerCustomizer;

  private final Supplier<Timer.Builder> timerBuilder;

  /**
   * Creates a wrapper for the given store which will record the time taken for each method call.
   * If the {@link MeterRegistry} is {@code null}, the store will not be wrapped and returned as is.
   */
  public static <T> T create(String storeType,
                             TypedStoreParameters<?> storeParameters,
                             Class<T> storeClass,
                             T store,
                             MeterRegistry meterRegistry) {
    if (meterRegistry == null) {
      return store;
    }
    return (T) Proxy.newProxyInstance(
      storeParameters.getType().getClassLoader(),
      new Class[]{storeClass},
      new StoreInvocationMicrometerWrapper(
        storeType,
        storeParameters,
        store,
        meterRegistry,
        () -> Timer.builder("scm.persistence")
      )
    );
  }

  /**
   * Creates a wrapper for the given store which will record the time taken for each method call.
   * If the {@link MeterRegistry} is {@code null}, the store will not be wrapped and returned as is.
   */
  public static <T, Q> Q create(String storeType,
                                Class<T> clazz,
                                String[] parentIds,
                                Class<Q> storeClass,
                                Q store,
                                MeterRegistry meterRegistry) {
    if (meterRegistry == null) {
      return store;
    }
    return (Q) Proxy.newProxyInstance(
      clazz.getClassLoader(),
      new Class[]{storeClass},
      new StoreInvocationMicrometerWrapper(
        storeType,
        clazz,
        parentIds,
        store,
        meterRegistry,
        () -> Timer.builder("scm.persistence")
      )
    );
  }

  @VisibleForTesting
  StoreInvocationMicrometerWrapper(String storeType,
                                   TypedStoreParameters<?> storeParameters,
                                   Object store,
                                   MeterRegistry meterRegistry,
                                   Supplier<Timer.Builder> timerBuilder) {
    this(
      meterRegistry,
      store,
      timer -> timer
        .description("Time taken for store operation in " + storeType)
        .tag("type", storeType)
        .tag("storeName", storeParameters.getName())
        .tag("repositoryId", storeParameters.getRepositoryId() == null ? "none" : storeParameters.getRepositoryId())
        .tag("namespace", storeParameters.getNamespace() == null ? "none" : storeParameters.getNamespace()),
      timerBuilder
    );
  }

  @VisibleForTesting
  <T, Q> StoreInvocationMicrometerWrapper(String storeType,
                                          Class<T> clazz,
                                          String[] parentIds,
                                          Q store,
                                          MeterRegistry meterRegistry,
                                          Supplier<Timer.Builder> timerBuilder) {
    this(
      meterRegistry,
      store,
      timer -> timer
        .description("Time taken for store operation in " + storeType)
        .tag("type", storeType)
        .tag("storeClass", clazz.getName())
        .tag("parentIds", String.join(",", parentIds)),
      timerBuilder
    );
  }

  private StoreInvocationMicrometerWrapper(MeterRegistry meterRegistry,
                                           Object store,
                                           Consumer<Timer.Builder> timerCustomizer,
                                           Supplier<Timer.Builder> timerBuilder) {
    AssertUtil.assertIsNotNull(store);
    this.meterRegistry = meterRegistry;
    this.store = store;
    this.timerCustomizer = timerCustomizer;
    this.timerBuilder = timerBuilder;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (meterRegistry == null) {
      return method.invoke(store, args);
    }
    if (QueryableStore.Query.class.isAssignableFrom(method.getReturnType())) {
      Object delegate = method.invoke(store, args);
      return Proxy.newProxyInstance(
        this.getClass().getClassLoader(),
        delegate.getClass().getInterfaces(),
        new StoreInvocationMicrometerWrapper(meterRegistry, delegate, timerCustomizer, timerBuilder)
      );
    }
    try {
      if (method.getName().equals("close")) {
        return method.invoke(store, args);
      }
      Timer.Builder timer = timerBuilder.get()
        .tag("method", method.getName());
      timerCustomizer.accept(timer);
      return timer
        .register(meterRegistry)
        .record(() -> {
          try {
            return method.invoke(store, args);
          } catch (Exception e) {
            throw new StoreException("Failed to invoke store method", e);
          }
        });
    } catch (InvocationTargetException | UndeclaredThrowableException e) {
      throw e.getCause();
    }
  }
}

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

import java.util.function.BooleanSupplier;

/**
 * Base class for {@link ConfigurationStore}.
 * Calls to {@link #get()}, {@link #set(Object)} and {@link #delete()} on the same
 * instance are serialized, including copying and persistence. Separate
 * read-modify-set sequences still require coordination by the caller.
 *
 * @param <T> type of store objects
 * @since 1.16
 */
public abstract class AbstractStore<T> implements ConfigurationStore<T> {

  private T storeObject;
  private boolean storeAlreadyRead = false;
  private final BooleanSupplier readOnly;

  protected AbstractStore(BooleanSupplier readOnly) {
    this.readOnly = readOnly;
  }

  @Override
  public synchronized T get() {
    if (!storeAlreadyRead) {
      storeObject = readObject();
      storeAlreadyRead = true;
    }

    return storeObject == null ? null : copy(storeObject);
  }

  @Override
  public synchronized void set(T object) {
    if (readOnly.getAsBoolean()) {
      throw new StoreReadOnlyException(object);
    }
    T snapshot = object == null ? null : copy(object);
    writeObject(snapshot);
    storeObject = snapshot;
    storeAlreadyRead = true;
  }


  @Override
  public synchronized void delete() {
    if (readOnly.getAsBoolean()) {
      throw new StoreReadOnlyException();
    }
    deleteObject();
    this.storeObject = null;
  }

  /**
   * Read the stored object.
   *
   * @return stored object
   */
  protected abstract T readObject();

  /**
   * Creates a deep copy of the stored state. The copy must not share mutable
   * objects with the original. This method is only called with non-null objects.
   *
   * @param object object to copy
   * @return independent copy of the stored state
   * @since 4.0.0
   */
  protected abstract T copy(T object);

  /**
   * Write object to the store.
   *
   * @param object object to write
   */
  protected abstract void writeObject(T object);

  /**
   * Deletes store object.
   *
   * @since 2.24.0
   */
  protected abstract void deleteObject();
}

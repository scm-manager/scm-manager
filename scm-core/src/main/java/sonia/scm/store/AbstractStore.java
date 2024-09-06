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
 *
 * @param <T> type of store objects
 * @since 1.16
 */
public abstract class AbstractStore<T> implements ConfigurationStore<T> {

  protected T storeObject;
  private boolean storeAlreadyRead = false;
  private final BooleanSupplier readOnly;

  protected AbstractStore(BooleanSupplier readOnly) {
    this.readOnly = readOnly;
  }

  @Override
  public T get() {
    if (!storeAlreadyRead) {
      storeObject = readObject();
      storeAlreadyRead = true;
    }

    return storeObject;
  }

  @Override
  public void set(T object) {
    if (readOnly.getAsBoolean()) {
      throw new StoreReadOnlyException(object);
    }
    writeObject(object);
    this.storeObject = object;
    storeAlreadyRead = true;
  }


  @Override
  public void delete() {
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

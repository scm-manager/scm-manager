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

package sonia.scm.store;

import java.util.function.BooleanSupplier;

/**
 * Base class for {@link ConfigurationStore}.
 *
 * @param <T> type of store objects
 * @author Sebastian Sdorra
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

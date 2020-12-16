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

/**
 * Base class for {@link ConfigurationStore}.
 *
 * @author Sebastian Sdorra
 * @since 1.16
 *
 * @param <T> type of store objects
 */
public abstract class AbstractStore<T> implements ConfigurationStore<T> {

  /**
   * stored object
   */
  protected T storeObject;
  private final boolean readOnly;

  protected AbstractStore(boolean readOnly) {
    this.readOnly = readOnly;
  }

  @Override
  public T get() {
    if (storeObject == null) {
      storeObject = readObject();
    }

    return storeObject;
  }

  @Override
  public void set(T object) {
    if (readOnly) {
      throw new StoreReadOnlyException(object);
    }
    writeObject(object);
    this.storeObject = object;
  }

  /**
   * Read the stored object.
   *
   *
   * @return stored object
   */
  protected abstract T readObject();

  /**
   * Write object to the store.
   *
   *
   * @param object object to write
   */
  protected abstract void writeObject(T object);
}

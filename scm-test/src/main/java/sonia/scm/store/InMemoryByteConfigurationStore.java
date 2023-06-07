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

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * In memory store implementation of {@link ConfigurationStore} using a byte array to store the serialized object.
 */
public class InMemoryByteConfigurationStore<T> implements ConfigurationStore<T> {

  private final Class<T> type;
  byte[] store;

  public InMemoryByteConfigurationStore(Class<T> type) {
    this.type = type;
  }

  @Override
  public T get() {
    if (store != null) {
      return JAXB.unmarshal(new ByteArrayInputStream(store), type);
    }
    return null;
  }

  @Override
  public void set(T object) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(object, baos);
    store = baos.toByteArray();
  }

  /**
   * This method can be used to mock stores with old types to test update steps or otherwise the compatability of
   * objects with old versions.
   */
  public void setOldObject(Object object) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(object, baos);
    store = baos.toByteArray();
  }
}

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

package sonia.scm.search;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import java.util.Optional;

@Singleton
public class DefaultIndexLogStore implements IndexLogStore {

  private final DataStore<IndexLog> dataStore;

  @Inject
  public DefaultIndexLogStore(DataStoreFactory dataStoreFactory) {
    this.dataStore = dataStoreFactory.withType(IndexLog.class).withName("index-log").build();
  }

  @Override
  public ForIndex forIndex(String index) {
    return new DefaultForIndex(index);
  }

  @Override
  public ForIndex defaultIndex() {
    // constant
    return new DefaultForIndex("default");
  }

  class DefaultForIndex implements ForIndex {

    private final String index;

    private DefaultForIndex(String index) {
      this.index = index;
    }

    private String id(Class<?> type) {
      return index + "_" + type.getName();
    }

    @Override
    public void log(Class<?> type, int version) {
      dataStore.put(id(type), new IndexLog(version));
    }

    @Override
    public Optional<IndexLog> get(Class<?> type) {
      return dataStore.getOptional(id(type));
    }
  }
}

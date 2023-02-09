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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.KeyGenerator;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.Map;

import static sonia.scm.store.CopyOnWrite.compute;

/**
 * Jaxb implementation of {@link DataStore}.
 *
 * @author Sebastian Sdorra
 *
 * @param <T> type of stored data.
 */
public class JAXBDataStore<T> extends FileBasedStore<T> implements DataStore<T> {

  /**
   * the logger for JAXBDataStore
   */
  private static final Logger LOG = LoggerFactory.getLogger(JAXBDataStore.class);

  private final KeyGenerator keyGenerator;
  private final TypedStoreContext<T> context;

  JAXBDataStore(KeyGenerator keyGenerator, TypedStoreContext<T> context, File directory, boolean readOnly) {
    super(directory, StoreConstants.FILE_EXTENSION, readOnly);
    this.keyGenerator = keyGenerator;
    this.directory = directory;
    this.context = context;
  }

  @Override
  public void put(String id, T item) {
    LOG.debug("put item {} to store", id);

    assertNotReadOnly();

    File file = getFile(id);

    try {
      Marshaller marshaller = context.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      CopyOnWrite.withTemporaryFile(
        temp -> marshaller.marshal(item, temp.toFile()),
        file.toPath()
      );
    }
    catch (JAXBException ex) {
      throw new StoreException("could not write object with id ".concat(id),
        ex);
    }
  }

  @Override
  public String put(T item) {
    String key = keyGenerator.createKey();

    put(key, item);

    return key;
  }

  @Override
  public Map<String, T> getAll() {
    LOG.trace("get all items from data store");

    Builder<String, T> builder = ImmutableMap.builder();

    for (File file : directory.listFiles()) {
      builder.put(getId(file), read(file));
    }

    return builder.build();
  }

  @Override
  protected T read(File file) {
    return compute(() -> {
      if (file.exists()) {
        LOG.trace("try to read {}", file);
        return context.unmarshall(file);
      }
      return null;
    }).withLockedFile(file);
  }
}

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

//~--- non-JDK imports --------------------------------------------------------
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.security.KeyGenerator;

//~--- JDK imports ------------------------------------------------------------
import java.io.File;

import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
  private static final Logger LOG
    = LoggerFactory.getLogger(JAXBDataStore.class);

  private final JAXBContext context;

  private final KeyGenerator keyGenerator;

  JAXBDataStore(KeyGenerator keyGenerator, Class<T> type, File directory) {
    super(directory, StoreConstants.FILE_EXTENSION);
    this.keyGenerator = keyGenerator;

    try {
      context = JAXBContext.newInstance(type);
      this.directory = directory;
    }
    catch (JAXBException ex) {
      throw new StoreException("failed to create jaxb context", ex);
    }
  }

  @Override
  public void put(String id, T item) {
    LOG.debug("put item {} to store", id);

    File file = getFile(id);

    try {
      Marshaller marshaller = context.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(item, file);
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
  @SuppressWarnings("unchecked")
  protected T read(File file) {
    T item = null;

    if (file.exists()) {
      LOG.trace("try to read {}", file);

      try {
        item = (T) context.createUnmarshaller().unmarshal(file);
      }
      catch (JAXBException ex) {
        throw new StoreException(
          "could not read object ".concat(file.getPath()), ex);
      }
    }

    return item;
  }
}

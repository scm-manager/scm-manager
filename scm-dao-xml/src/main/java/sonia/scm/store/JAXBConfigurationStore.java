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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.function.BooleanSupplier;

import static sonia.scm.store.CopyOnWrite.compute;
import static sonia.scm.store.CopyOnWrite.execute;

/**
 * JAXB implementation of {@link ConfigurationStore}.
 *
 * @param <T>
 */
public class JAXBConfigurationStore<T> extends AbstractStore<T> {

 
  private static final Logger LOG = LoggerFactory.getLogger(JAXBConfigurationStore.class);

  private final TypedStoreContext<T> context;
  private final Class<T> type;
  private final File configFile;

  public JAXBConfigurationStore(TypedStoreContext<T> context, Class<T> type, File configFile, BooleanSupplier readOnly) {
    super(readOnly);
    this.context = context;
    this.type = type;
    this.configFile = configFile;
  }

  public Class<T> getType() {
    return type;
  }

  @Override
  protected T readObject() {
    LOG.debug("load {} from store {}", type, configFile);

    return compute(
      () -> {
        if (configFile.exists()) {
          return context.unmarshal(configFile);
        }
        return null;
      }
    ).withLockedFileForRead(configFile);
  }

  @Override
  protected void writeObject(T object) {
    LOG.debug("store {} to {}", object.getClass().getName(), configFile.getPath());
    CopyOnWrite.withTemporaryFile(
      temp -> context.marshal(object, temp.toFile()),
      configFile.toPath()
    );
  }

  @Override
  protected void deleteObject() {
    LOG.debug("deletes {}", configFile.getPath());
    execute(() -> {
      try {
        IOUtil.delete(configFile);
      } catch (IOException e) {
        throw new StoreException("Failed to delete store object " + configFile.getPath(), e);
      }
    }).withLockedFileForWrite(configFile);
  }
}

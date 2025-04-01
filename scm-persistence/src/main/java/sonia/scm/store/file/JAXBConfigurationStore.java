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

package sonia.scm.store.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.CopyOnWrite;
import sonia.scm.store.AbstractStore;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.StoreException;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.function.BooleanSupplier;

import static sonia.scm.CopyOnWrite.compute;
import static sonia.scm.CopyOnWrite.execute;

/**
 * JAXB implementation of {@link ConfigurationStore}.
 *
 * @param <T>
 */
class JAXBConfigurationStore<T> extends AbstractStore<T> {

 
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

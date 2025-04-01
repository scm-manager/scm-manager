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


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.store.MultiEntryStore;
import sonia.scm.store.StoreException;
import sonia.scm.store.StoreReadOnlyException;

import java.io.File;


abstract class FileBasedStore<T> implements MultiEntryStore<T> {

  private static final Logger logger =
    LoggerFactory.getLogger(FileBasedStore.class);

  public FileBasedStore(File directory, String suffix, boolean readOnly)
  {
    this.directory = directory;
    this.suffix = suffix;
    this.readOnly = readOnly;
  }



  protected abstract T read(File file);

   @Override
  public void clear()
  {
    logger.debug("clear store");

    for (File file : directory.listFiles())
    {
      remove(file);
    }
  }


  @Override
  public void remove(String id)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
      "id argument is required");
    logger.debug("try to delete store entry with id {}", id);

    File file = getFile(id);

    remove(file);
  }



  @Override
  public T get(String id)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
      "id argument is required");
    logger.trace("try to retrieve item with id {}", id);

    File file = getFile(id);

    return read(file);
  }



  protected void remove(File file)
  {
    logger.trace("delete store entry {}", file);

    assertNotReadOnly();

    if (file.exists() &&!file.delete())
    {
      throw new StoreException(
        "could not delete store entry ".concat(file.getPath()));
    }
  }



  protected File getFile(String id)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
      "id argument is required");

    return new File(directory, id.concat(suffix));
  }


  protected String getId(File file)
  {
    String name = file.getName();

    return name.substring(0, name.length() - suffix.length());
  }

  protected void assertNotReadOnly() {
    if (readOnly) {
      throw new StoreReadOnlyException(directory.getAbsoluteFile().toString());
    }
  }

  //~--- fields ---------------------------------------------------------------

  protected File directory;

  private final String suffix;

  private final boolean readOnly;
}

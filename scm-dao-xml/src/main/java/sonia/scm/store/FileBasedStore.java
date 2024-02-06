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


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public abstract class FileBasedStore<T> implements MultiEntryStore<T>
{

 
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

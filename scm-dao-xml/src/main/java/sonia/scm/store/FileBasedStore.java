/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class FileBasedStore<T> implements StoreBase<T>
{

  /**
   * the logger for FileBasedStore
   */
  private static final Logger logger =
    LoggerFactory.getLogger(FileBasedStore.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param directory
   * @param suffix
   */
  public FileBasedStore(File directory, String suffix)
  {
    this.directory = directory;
    this.suffix = suffix;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   */
  protected abstract T read(File file);

  /**
   * Method description
   *
   */
  @Override
  public void clear()
  {
    logger.debug("clear store");

    for (File file : directory.listFiles())
    {
      remove(file);
    }
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void remove(String id)
  {
    File file = getFile(id);

    remove(file);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public T get(String id)
  {
    logger.trace("try to retrieve item with id {}", id);

    File file = getFile(id);

    return read(file);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   */
  protected void remove(File file)
  {
    if (file.exists() &&!file.delete())
    {
      logger.debug("delete store entry {}", file);

      throw new StoreException(
        "could not delete store entry ".concat(file.getPath()));
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  protected File getFile(String id)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
      "id argument is required");

    return new File(directory, id.concat(suffix));
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   */
  protected String getId(File file)
  {
    String name = file.getName();

    return name.substring(0, name.length() - suffix.length());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected File directory;

  /** Field description */
  private String suffix;
}

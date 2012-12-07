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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.security.KeyGenerator;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class FileBlobStore extends FileBasedStore<Blob> implements BlobStore
{

  /** Field description */
  private static final String SUFFIX = ".blob";

  /**
   * the logger for FileBlobStore
   */
  private static final Logger logger =
    LoggerFactory.getLogger(FileBlobStore.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param keyGenerator
   * @param directory
   */
  public FileBlobStore(KeyGenerator keyGenerator, File directory)
  {
    super(directory, SUFFIX);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Blob create()
  {
    return create(keyGenerator.createKey());
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public Blob create(String id)
  {
    return new FileBlob(id, getFile(id));
  }

  /**
   * Method description
   *
   *
   * @param blob
   */
  @Override
  public void remove(Blob blob)
  {
    remove(blob.getId());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<Blob> getAll()
  {
    logger.trace("get all items from data store");

    Builder<Blob> builder = ImmutableList.builder();

    for (File file : directory.listFiles())
    {
      builder.add(read(file));
    }

    return builder.build();
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
  @Override
  protected FileBlob read(File file)
  {
    String id = getId(file);

    return new FileBlob(id, file);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private KeyGenerator keyGenerator;
}

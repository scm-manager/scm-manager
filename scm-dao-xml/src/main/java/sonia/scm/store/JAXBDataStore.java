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

import sonia.scm.security.KeyGenerator;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public class JAXBDataStore<T> extends AbstractDataStore<T>
{

  /** Field description */
  private static final String SUFFIX = ".xml";

  /**
   * the logger for JAXBDataStore
   */
  private static final Logger logger =
    LoggerFactory.getLogger(JAXBDataStore.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param type
   * @param keyGenerator
   * @param directory
   */
  public JAXBDataStore(KeyGenerator keyGenerator, Class<T> type, File directory)
  {
    super(keyGenerator);

    try
    {
      context = JAXBContext.newInstance(type);
      this.directory = directory;
    }
    catch (JAXBException ex)
    {
      throw new StoreException(ex);
    }
  }

  //~--- methods --------------------------------------------------------------

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
   * @param item
   */
  @Override
  public void put(String id, T item)
  {
    logger.info("put item {} to store", id);

    File file = getFile(id);

    if (file.exists())
    {
      try
      {
        Marshaller marshaller = context.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(item, file);
      }
      catch (JAXBException ex)
      {
        throw new StoreException("could not write object with id ".concat(id),
          ex);
      }
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

    T item = null;
    File file = getFile(id);

    if (file.exists())
    {
      try
      {
        item = (T) context.createUnmarshaller().unmarshal(file);
      }
      catch (JAXBException ex)
      {
        throw new StoreException("could not read object with id ".concat(id),
          ex);
      }
    }

    return item;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   */
  private void remove(File file)
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
  private File getFile(String id)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
      "id argument is required");

    return new File(directory, id.concat(SUFFIX));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private File directory;
}

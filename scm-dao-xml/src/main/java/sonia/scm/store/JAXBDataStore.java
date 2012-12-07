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
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public class JAXBDataStore<T> extends FileBasedStore<T> implements DataStore<T>
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
    super(directory, SUFFIX);
    this.keyGenerator = keyGenerator;

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
   *
   * @param id
   * @param item
   */
  @Override
  public void put(String id, T item)
  {
    logger.debug("put item {} to store", id);

    File file = getFile(id);

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

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  @Override
  public String put(T item)
  {
    String key = keyGenerator.createKey();

    put(key, item);

    return key;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Map<String, T> getAll()
  {
    logger.trace("get all items from data store");

    Builder<String, T> builder = ImmutableMap.builder();

    for (File file : directory.listFiles())
    {
      builder.put(getId(file), read(file));
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
  protected T read(File file)
  {
    T item = null;

    if (file.exists())
    {
      logger.trace("try to read {}", file);

      try
      {
        item = (T) context.createUnmarshaller().unmarshal(file);
      }
      catch (JAXBException ex)
      {
        throw new StoreException(
          "could not read object ".concat(file.getPath()), ex);
      }
    }

    return item;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private KeyGenerator keyGenerator;
}

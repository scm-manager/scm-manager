/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class JAXBStore<T> extends AbstractListenableStore<T>
{

  /** the logger for JAXBStore */
  private static final Logger logger = LoggerFactory.getLogger(JAXBStore.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param type
   * @param configFile
   */
  public JAXBStore(Class<T> type, File configFile)
  {
    this.type = type;

    try
    {
      context = JAXBContext.newInstance(type);
      this.configFile = configFile;
    }
    catch (JAXBException ex)
    {
      throw new StoreException(ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public T get()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("load {} from store {}", type, configFile);
    }

    T result = null;

    if (configFile.exists())
    {
      try
      {
        result = (T) context.createUnmarshaller().unmarshal(configFile);
      }
      catch (JAXBException ex)
      {
        throw new StoreException(ex);
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Class<T> getType()
  {
    return type;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param object
   */
  @Override
  public void set(T object)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("store {} to {}", object.getClass().getName(),
                   configFile.getPath());
    }

    try
    {
      Marshaller marshaller = context.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(object, configFile);
      
      fireEvent(object);
    }
    catch (JAXBException ex)
    {
      throw new StoreException(ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File configFile;

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private Class<T> type;
}

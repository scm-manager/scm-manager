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



package sonia.scm.store.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.orientdb.OrientDBUtil;
import sonia.scm.store.Store;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public class OrientDBStore<T> implements Store<T>
{

  /** Field description */
  public static final String DOCUMENT_CLASS = "StoreObject";

  /** Field description */
  public static final String FIELD_DATA = "data";

  /** Field description */
  public static final String FIELD_NAME = "name";

  /** Field description */
  public static final String FIELD_TYPE = "type";

  /** Field description */
  public static final String INDEX_NAME = "StoreTypeAndName";

  /** Field description */
  public static final String QUERY_STORE =
    "select from StoreObject where name = ? and type = ?";

  /**
   * the logger for OrientDBStore
   */
  private static final Logger logger =
    LoggerFactory.getLogger(OrientDBStore.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param connectionProvider
   * @param context
   * @param type
   * @param name
   */
  public OrientDBStore(Provider<ODatabaseDocumentTx> connectionProvider,
                       JAXBContext context, Class<T> type, String name)
  {
    this.connectionProvider = connectionProvider;
    this.context = context;
    this.type = type;
    this.name = name;
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
    T result = null;
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      ODocument doc = getStoreDocument(connection);

      if (doc != null)
      {
        String data = doc.field(FIELD_DATA);

        if (Util.isNotEmpty(data))
        {
          StringReader reader = new StringReader(data);

          result = (T) context.createUnmarshaller().unmarshal(reader);
        }
      }
    }
    catch (JAXBException ex)
    {
      logger.error("could not unmarshall object", ex);
    }
    finally
    {
      OrientDBUtil.close(connection);
    }

    return result;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param t
   */
  public void set(T t)
  {
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      ODocument doc = getStoreDocument(connection);

      if (doc == null)
      {
        doc = new ODocument(DOCUMENT_CLASS);
        doc.field(FIELD_NAME, name);
        doc.field(FIELD_TYPE, type.getName());
      }

      StringWriter buffer = new StringWriter();

      context.createMarshaller().marshal(t, buffer);
      doc.field(FIELD_DATA, buffer.toString());
      doc.save();
    }
    catch (JAXBException ex)
    {
      logger.error("could not marshall object", ex);
    }
    finally
    {
      OrientDBUtil.close(connection);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param connection
   *
   * @return
   */
  private ODocument getStoreDocument(ODatabaseDocumentTx connection)
  {
    return OrientDBUtil.executeSingleResultQuery(connection, QUERY_STORE, name,
            type.getName());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<ODatabaseDocumentTx> connectionProvider;

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private String name;

  /** Field description */
  private Class<T> type;
}

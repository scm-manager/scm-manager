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



package sonia.scm.store.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;

import sonia.scm.SCMContextProvider;
import sonia.scm.orientdb.OrientDBUtil;
import sonia.scm.store.ListenableStoreFactory;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class OrientDBStoreFactory implements ListenableStoreFactory
{

  /**
   * Constructs ...
   *
   *
   * @param connectionProvider
   */
  @Inject
  public OrientDBStoreFactory(Provider<ODatabaseDocumentTx> connectionProvider)
  {
    this.connectionProvider = connectionProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      OSchema schema = connection.getMetadata().getSchema();
      OClass oclass = schema.getClass(OrientDBStore.DOCUMENT_CLASS);

      if (oclass == null)
      {
        oclass = schema.createClass(OrientDBStore.DOCUMENT_CLASS);
        oclass.createProperty(OrientDBStore.FIELD_NAME, OType.STRING);
        oclass.createProperty(OrientDBStore.FIELD_TYPE, OType.STRING);
        oclass.createProperty(OrientDBStore.FIELD_DATA, OType.STRING);
        oclass.createIndex(OrientDBStore.INDEX_NAME, INDEX_TYPE.UNIQUE,
                           OrientDBStore.FIELD_NAME, OrientDBStore.FIELD_TYPE);
        schema.save();
      }
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
   * @param type
   * @param name
   * @param <T>
   *
   * @return
   */
  @Override
  public <T> OrientDBStore<T> getStore(Class<T> type, String name)
  {
    AssertUtil.assertIsNotNull(type);
    AssertUtil.assertIsNotEmpty(name);

    try
    {
      return new OrientDBStore<T>(connectionProvider,
                               JAXBContext.newInstance(type), type, name);
    }
    catch (JAXBException ex)
    {
      throw new RuntimeException(
          "could not create jaxb context for ".concat(type.getName()), ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<ODatabaseDocumentTx> connectionProvider;
}

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



package sonia.scm.user.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;

import sonia.scm.orientdb.AbstractOrientDBModelDAO;
import sonia.scm.orientdb.OrientDBUtil;
import sonia.scm.repository.orientdb.RepositoryConverter;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class OrientDBUserDAO extends AbstractOrientDBModelDAO<User>
        implements UserDAO
{

  /** Field description */
  public static final String QUERY_ALL = "select from User";

  /** Field description */
  public static final String QUERY_SINGLE_BYID =
    "select from User where id = ?";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param connectionProvider
   */
  public OrientDBUserDAO(Provider<ODatabaseDocumentTx> connectionProvider)
  {
    super(connectionProvider, UserConverter.INSTANCE);
    init();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  public void init()
  {
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      OSchema schema = connection.getMetadata().getSchema();
      OClass oclass = schema.getClass(UserConverter.DOCUMENT_CLASS);

      if (oclass == null)
      {
        UserConverter.INSTANCE.createShema(connection);
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
   * @param connection
   *
   * @return
   */
  @Override
  protected List<ODocument> getAllDocuments(ODatabaseDocumentTx connection)
  {
    return OrientDBUtil.executeListResultQuery(connection, QUERY_ALL);
  }

  /**
   * Method description
   *
   *
   * @param connection
   * @param id
   *
   * @return
   */
  @Override
  protected ODocument getDocument(ODatabaseDocumentTx connection, String id)
  {
    return OrientDBUtil.executeSingleResultQuery(connection, QUERY_SINGLE_BYID,
            id);
  }
}

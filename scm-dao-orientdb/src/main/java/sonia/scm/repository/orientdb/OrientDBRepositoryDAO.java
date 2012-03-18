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



package sonia.scm.repository.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import sonia.scm.orientdb.AbstractOrientDBModelDAO;
import sonia.scm.orientdb.OrientDBUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class OrientDBRepositoryDAO extends AbstractOrientDBModelDAO<Repository>
        implements RepositoryDAO
{

  /** Field description */
  public static final String QUERY_ALL = "select from Repository";

  /** Field description */
  public static final String QUERY_SINGLE_BYID =
    "select from Repository where id = ?";

  /** Field description */
  public static final String QUERY_SINGLE_BYTYPEANDNAME =
    "select from Repository where type = ? and name = ?";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param connectionProvider
   */
  @Inject
  public OrientDBRepositoryDAO(Provider<ODatabaseDocumentTx> connectionProvider)
  {
    super(connectionProvider, RepositoryConverter.INSTANCE);
    init();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   *
   * @return
   */
  @Override
  public boolean contains(String type, String name)
  {
    return get(type, name) != null;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   *
   * @return
   */
  @Override
  public Repository get(String type, String name)
  {
    Repository repository = null;
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      ODocument doc = getDocument(connection, type, name);

      if (doc != null)
      {
        repository = converter.convert(doc);
      }
    }
    finally
    {
      OrientDBUtil.close(connection);
    }

    return repository;
  }

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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   */
  private void init()
  {
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      OSchema schema = connection.getMetadata().getSchema();
      OClass oclass = schema.getClass(RepositoryConverter.DOCUMENT_CLASS);

      if (oclass == null)
      {
        oclass = schema.createClass(RepositoryConverter.DOCUMENT_CLASS);

        // model properites
        oclass.createProperty(RepositoryConverter.FIELD_ID, OType.STRING);
        oclass.createProperty(RepositoryConverter.FIELD_TYPE, OType.STRING);
        oclass.createProperty(RepositoryConverter.FIELD_LASTMODIFIED,
                              OType.LONG);

        // repository properties
        oclass.createProperty(RepositoryConverter.FIELD_CONTACT, OType.STRING);
        oclass.createProperty(RepositoryConverter.FIELD_CREATIONDATE,
                              OType.LONG);
        oclass.createProperty(RepositoryConverter.FIELD_DESCRIPTION,
                              OType.STRING);
        oclass.createProperty(RepositoryConverter.FIELD_NAME, OType.STRING);
        oclass.createProperty(RepositoryConverter.FIELD_PERMISSIONS,
                              OType.EMBEDDEDLIST);
        oclass.createProperty(RepositoryConverter.FIELD_PROPERTIES,
                              OType.EMBEDDEDMAP);
        oclass.createProperty(RepositoryConverter.FIELD_PUBLIC, OType.BOOLEAN);

        // indexes
        oclass.createIndex(RepositoryConverter.INDEX_ID, INDEX_TYPE.UNIQUE,
                           RepositoryConverter.FIELD_ID);
        oclass.createIndex(RepositoryConverter.INDEX_TYPEANDNAME,
                           INDEX_TYPE.UNIQUE, RepositoryConverter.FIELD_NAME,
                           RepositoryConverter.FIELD_TYPE);
        schema.save();
      }

      oclass = schema.getClass(PermissionConverter.DOCUMENT_CLASS);

      if (oclass == null)
      {
        oclass = schema.createClass(PermissionConverter.DOCUMENT_CLASS);
        oclass.createProperty(PermissionConverter.FIELD_NAME, OType.STRING);
        oclass.createProperty(PermissionConverter.FIELD_TYPE, OType.STRING);
        oclass.createProperty(PermissionConverter.FIELD_GROUP, OType.BOOLEAN);
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
   * @param connection
   * @param type
   * @param name
   *
   * @return
   */
  private ODocument getDocument(ODatabaseDocumentTx connection, String type,
                                String name)
  {
    return OrientDBUtil.executeSingleResultQuery(connection,
            QUERY_SINGLE_BYTYPEANDNAME, type, name);
  }
}

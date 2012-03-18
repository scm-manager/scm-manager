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

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import sonia.scm.orientdb.AbstractConverter;
import sonia.scm.orientdb.Converter;
import sonia.scm.orientdb.OrientDBUtil;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryConverter extends AbstractConverter
        implements Converter<Repository>
{

  /** Field description */
  public static final String DOCUMENT_CLASS = "Repository";

  /** Field description */
  public static final String FIELD_CONTACT = "contact";

  /** Field description */
  public static final String FIELD_CREATIONDATE = "creationDate";

  /** Field description */
  public static final String FIELD_DESCRIPTION = "description";

  /** Field description */
  public static final String FIELD_NAME = "name";

  /** Field description */
  public static final String FIELD_PERMISSIONS = "permissions";

  /** Field description */
  public static final String FIELD_PUBLIC = "public";

  /** Field description */
  public static final String INDEX_ID = "RepositoryId";

  /** Field description */
  public static final String INDEX_TYPEANDNAME = "RepositoryTypeAndName";

  /** Field description */
  public static final RepositoryConverter INSTANCE = new RepositoryConverter();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param doc
   * @param repository
   *
   * @return
   */
  @Override
  public ODocument convert(ODocument doc, Repository repository)
  {
    appendModelObjectFields(doc, repository);
    appendField(doc, FIELD_NAME, repository.getName());
    appendField(doc, FIELD_CONTACT, repository.getContact());
    appendField(doc, FIELD_DESCRIPTION, repository.getDescription());
    appendField(doc, FIELD_PUBLIC, repository.isPublicReadable(),
                OType.BOOLEAN);
    appendField(doc, FIELD_CREATIONDATE, repository.getCreationDate(),
                OType.LONG);
    appendPropertiesField(doc, repository);
    appendListField(doc, FIELD_PERMISSIONS, PermissionConverter.INSTANCE,
                    repository.getPermissions());

    return doc;
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  @Override
  public ODocument convert(Repository repository)
  {
    ODocument doc = new ODocument(DOCUMENT_CLASS);

    convert(doc, repository);

    return doc;
  }

  /**
   * Method description
   *
   *
   * @param doc
   *
   * @return
   */
  @Override
  public Repository convert(ODocument doc)
  {
    Repository repository = new Repository();

    repository.setId(getStringField(doc, FIELD_ID));
    repository.setType(getStringField(doc, FIELD_TYPE));
    repository.setName(getStringField(doc, FIELD_NAME));
    repository.setContact(getStringField(doc, FIELD_CONTACT));
    repository.setDescription(getStringField(doc, FIELD_DESCRIPTION));
    repository.setPublicReadable(getBooleanField(doc, FIELD_PUBLIC));
    repository.setLastModified(getLongField(doc, FIELD_LASTMODIFIED));
    repository.setCreationDate(getLongField(doc, FIELD_CREATIONDATE));

    Map<String, String> properties = doc.field(FIELD_PROPERTIES);

    repository.setProperties(properties);

    List<ODocument> permissions = doc.field(FIELD_PERMISSIONS);

    repository.setPermissions(
        OrientDBUtil.transformToItems(
          PermissionConverter.INSTANCE, permissions));

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param connection
   */
  @Override
  public void createShema(ODatabaseDocumentTx connection)
  {
    OSchema schema = connection.getMetadata().getSchema();
    OClass oclass = schema.getClass(DOCUMENT_CLASS);

    if (oclass == null)
    {
      oclass = schema.createClass(DOCUMENT_CLASS);

      // model properites
      oclass.createProperty(FIELD_ID, OType.STRING);
      oclass.createProperty(FIELD_TYPE, OType.STRING);
      oclass.createProperty(FIELD_LASTMODIFIED, OType.LONG);

      // repository properties
      oclass.createProperty(FIELD_CONTACT, OType.STRING);
      oclass.createProperty(FIELD_CREATIONDATE, OType.LONG);
      oclass.createProperty(FIELD_DESCRIPTION, OType.STRING);
      oclass.createProperty(FIELD_NAME, OType.STRING);
      oclass.createProperty(FIELD_PERMISSIONS, OType.EMBEDDEDLIST);
      oclass.createProperty(FIELD_PROPERTIES, OType.EMBEDDEDMAP);
      oclass.createProperty(FIELD_PUBLIC, OType.BOOLEAN);

      // indexes
      oclass.createIndex(INDEX_ID, INDEX_TYPE.UNIQUE, FIELD_ID);
      oclass.createIndex(INDEX_TYPEANDNAME, INDEX_TYPE.UNIQUE, FIELD_NAME,
                         FIELD_TYPE);
      schema.save();
    }

    PermissionConverter.INSTANCE.createShema(connection);
  }
}

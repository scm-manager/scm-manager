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
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import sonia.scm.orientdb.AbstractConverter;
import sonia.scm.orientdb.Converter;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;

/**
 *
 * @author Sebastian Sdorra
 */
public class PermissionConverter extends AbstractConverter
        implements Converter<Permission>
{

  /** Field description */
  public static final String DOCUMENT_CLASS = "Permission";

  /** Field description */
  public static final String FIELD_GROUP = "group";

  /** Field description */
  public static final String FIELD_NAME = "name";

  /** Field description */
  public static final PermissionConverter INSTANCE = new PermissionConverter();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param doc
   * @param permission
   *
   * @return
   */
  @Override
  public ODocument convert(ODocument doc, Permission permission)
  {
    appendField(doc, FIELD_NAME, permission.getName());
    appendField(doc, FIELD_TYPE, permission.getType().toString());
    appendField(doc, FIELD_GROUP, permission.isGroupPermission(),
                OType.BOOLEAN);

    return doc;
  }

  /**
   * Method description
   *
   *
   * @param permission
   *
   * @return
   */
  @Override
  public ODocument convert(Permission permission)
  {
    ODocument doc = new ODocument(DOCUMENT_CLASS);

    convert(doc, permission);

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
  public Permission convert(ODocument doc)
  {
    Permission permission = new Permission();

    permission.setName(getStringField(doc, FIELD_NAME));

    String typeString = doc.field(FIELD_TYPE);

    if (typeString != null)
    {
      permission.setType(PermissionType.valueOf(typeString));
    }

    permission.setGroupPermission(getBooleanField(doc, FIELD_GROUP));

    return permission;
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
      oclass.createProperty(FIELD_NAME, OType.STRING);
      oclass.createProperty(FIELD_TYPE, OType.STRING);
      oclass.createProperty(FIELD_GROUP, OType.BOOLEAN);
      schema.save();
    }
  }
}

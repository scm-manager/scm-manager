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



package sonia.scm.group.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import sonia.scm.group.Group;
import sonia.scm.orientdb.AbstractConverter;
import sonia.scm.orientdb.Converter;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class GroupConverter extends AbstractConverter
        implements Converter<Group>
{

  /** Field description */
  public static final String DOCUMENT_CLASS = "Group";

  /** Field description */
  public static final String FIELD_CREATIONDATE = "creationDate";

  /** Field description */
  public static final String FIELD_DESCRIPTION = "description";

  /** Field description */
  public static final String FIELD_MEMBERS = "members";

  /** Field description */
  public static final String INDEX_ID = "groupId";

  /** Field description */
  public static final GroupConverter INSTANCE = new GroupConverter();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param group
   *
   * @return
   */
  @Override
  public ODocument convert(Group group)
  {
    ODocument doc = new ODocument(DOCUMENT_CLASS);

    return convert(doc, group);
  }

  /**
   * Method description
   *
   *
   * @param doc
   * @param group
   *
   * @return
   */
  @Override
  public ODocument convert(ODocument doc, Group group)
  {
    appendModelObjectFields(doc, group);
    appendField(doc, FIELD_DESCRIPTION, group.getDescription());
    appendField(doc, FIELD_CREATIONDATE, group.getCreationDate(), OType.LONG);
    appendField(doc, FIELD_MEMBERS, group.getMembers(), OType.EMBEDDEDLIST);
    appendPropertiesField(doc, group);

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
  public Group convert(ODocument doc)
  {
    Group group = new Group();

    group.setName(getStringField(doc, FIELD_ID));
    group.setDescription(getStringField(doc, FIELD_DESCRIPTION));
    group.setType(getStringField(doc, FIELD_TYPE));
    group.setCreationDate(getLongField(doc, FIELD_CREATIONDATE));
    group.setLastModified(getLongField(doc, FIELD_LASTMODIFIED));

    Map<String, String> properties = doc.field(FIELD_PROPERTIES);

    group.setProperties(properties);

    List<String> members = doc.field(FIELD_MEMBERS);

    group.setMembers(members);

    return group;
  }

  /**
   * Method description
   *
   *
   * @param connection
   */
  void createShema(ODatabaseDocumentTx connection)
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

      // user properties
      oclass.createProperty(FIELD_DESCRIPTION, OType.STRING);
      oclass.createProperty(FIELD_CREATIONDATE, OType.LONG);
      oclass.createProperty(FIELD_MEMBERS, OType.EMBEDDEDLIST);
      oclass.createProperty(FIELD_PROPERTIES, OType.EMBEDDEDMAP);

      // indexes
      oclass.createIndex(INDEX_ID, INDEX_TYPE.UNIQUE, FIELD_ID);
      schema.save();
    }
  }
}

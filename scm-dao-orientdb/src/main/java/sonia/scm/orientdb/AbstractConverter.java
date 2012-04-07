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


package sonia.scm.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import sonia.scm.ModelObject;
import sonia.scm.PropertiesAware;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractConverter
{

  /** Field description */
  public static final String FIELD_ID = "id";

  /** Field description */
  public static final String FIELD_LASTMODIFIED = "lastModified";

  /** Field description */
  public static final String FIELD_PROPERTIES = "properties";

  /** Field description */
  public static final String FIELD_TYPE = "type";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param doc
   * @param name
   * @param value
   */
  protected void appendField(ODocument doc, String name, Object value)
  {
    appendField(doc, name, value, null);
  }

  /**
   * Method description
   *
   *
   * @param doc
   * @param name
   * @param value
   * @param type
   */
  protected void appendField(ODocument doc, String name, Object value,
                             OType type)
  {
    if (value != null)
    {
      if (type != null)
      {
        doc.field(name, value, type);
      }
      else
      {
        doc.field(name, value);
      }
    }
    else if (doc.containsField(name))
    {
      doc.removeField(name);
    }
  }

  /**
   * Method description
   *
   *
   * @param doc
   * @param name
   * @param converter
   * @param list
   * @param <T>
   */
  protected <T> void appendListField(ODocument doc, String name,
                                     Converter<T> converter, List<T> list)
  {
    List<ODocument> docs = OrientDBUtil.transformToDocuments(converter, list);

    appendField(doc, name, docs, OType.EMBEDDEDLIST);
  }

  /**
   * Method description
   *
   *
   * @param doc
   * @param model
   */
  protected void appendModelObjectFields(ODocument doc, ModelObject model)
  {
    appendField(doc, FIELD_ID, model.getId());
    appendField(doc, FIELD_TYPE, model.getType());
    appendField(doc, FIELD_LASTMODIFIED, model.getLastModified(), OType.LONG);
  }

  /**
   * Method description
   *
   *
   * @param doc
   * @param object
   */
  protected void appendPropertiesField(ODocument doc, PropertiesAware object)
  {
    appendField(doc, FIELD_PROPERTIES, object.getProperties(),
                OType.EMBEDDEDMAP);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param doc
   * @param name
   *
   * @return
   */
  protected Boolean getBooleanField(ODocument doc, String name)
  {
    return doc.field(name);
  }

  /**
   * Method description
   *
   *
   * @param doc
   * @param name
   *
   * @return
   */
  protected Long getLongField(ODocument doc, String name)
  {
    return doc.field(name);
  }

  /**
   * Method description
   *
   *
   * @param doc
   * @param name
   *
   * @return
   */
  protected String getStringField(ODocument doc, String name)
  {
    return doc.field(name);
  }
}

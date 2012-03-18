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



package sonia.scm.orientdb;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.inject.Provider;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import sonia.scm.GenericDAO;
import sonia.scm.ModelObject;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class AbstractOrientDBModelDAO<T extends ModelObject>
        implements GenericDAO<T>
{

  /** Field description */
  public static final String TYPE = "orientdb";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param connectionProvider
   * @param converter
   */
  public AbstractOrientDBModelDAO(
          Provider<ODatabaseDocumentTx> connectionProvider,
          Converter<T> converter)
  {
    this.connectionProvider = connectionProvider;
    this.converter = converter;
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
  protected abstract List<ODocument> getAllDocuments(
          ODatabaseDocumentTx connection);

  /**
   * Method description
   *
   *
   * @param connection
   * @param id
   *
   * @return
   */
  protected abstract ODocument getDocument(ODatabaseDocumentTx connection,
          String id);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param item
   */
  @Override
  public void add(T item)
  {
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      ODocument doc = converter.convert(item);

      doc.save();
    }
    finally
    {
      OrientDBUtil.close(connection);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param item
   *
   * @return
   */
  @Override
  public boolean contains(T item)
  {
    return contains(item.getId());
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public boolean contains(String id)
  {
    return get(id) != null;
  }

  /**
   * Method description
   *
   *
   * @param item
   */
  @Override
  public void delete(T item)
  {
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      ODocument doc = getDocument(connection, item.getId());

      if (doc != null)
      {
        doc.delete();
      }
    }
    finally
    {
      OrientDBUtil.close(connection);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param item
   */
  @Override
  public void modify(T item)
  {
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      ODocument doc = getDocument(connection, item.getId());

      if (doc != null)
      {
        doc = converter.convert(doc, item);
        doc.save();
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
   * @param id
   *
   * @return
   */
  @Override
  public T get(String id)
  {
    T item = null;
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      ODocument doc = getDocument(connection, id);

      if (doc != null)
      {
        item = converter.convert(doc);
      }
    }
    finally
    {
      OrientDBUtil.close(connection);
    }

    return item;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<T> getAll()
  {
    List<T> items = null;
    ODatabaseDocumentTx connection = connectionProvider.get();

    try
    {
      List<ODocument> result = getAllDocuments(connection);

      if (Util.isNotEmpty(result))
      {
        items = OrientDBUtil.transformToItems(converter, result);
      }
      else
      {
        items = Lists.newArrayList();
      }
    }
    finally
    {
      OrientDBUtil.close(connection);
    }

    return items;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Long getCreationTime()
  {

    // TODO
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Long getLastModified()
  {

    // TODO
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getType()
  {
    return TYPE;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected Provider<ODatabaseDocumentTx> connectionProvider;

  /** Field description */
  protected Converter<T> converter;
}

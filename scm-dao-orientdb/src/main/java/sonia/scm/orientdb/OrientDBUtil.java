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

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class OrientDBUtil
{

  /** Field description */
  public static final String FETCH_PLAN = "*:-1";

  /**
   * the logger for OrientDBUtil
   */
  private static final Logger logger =
    LoggerFactory.getLogger(OrientDBUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param connection
   */
  public static void close(ODatabaseDocumentTx connection)
  {
    if (connection != null)
    {
      connection.close();
    }
  }

  /**
   * Method description
   *
   *
   * @param connection
   * @param query
   * @param parameters
   *
   * @return
   */
  public static List<ODocument> executeListResultQuery(
          ODatabaseDocumentTx connection, String query, Object... parameters)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("execute list result query '{}'", query);
    }

    OSQLSynchQuery<ODocument> osqlQuery = new OSQLSynchQuery<ODocument>(query);

    osqlQuery.setFetchPlan(FETCH_PLAN);

    return connection.command(osqlQuery).execute(parameters);
  }

  /**
   * Method description
   *
   *
   * @param connection
   * @param query
   * @param parameters
   *
   * @return
   */
  public static ODocument executeSingleResultQuery(
          ODatabaseDocumentTx connection, String query, Object... parameters)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("execute single result query '{}'", query);
    }

    ODocument result = null;
    OSQLSynchQuery<ODocument> osqlQuery = new OSQLSynchQuery<ODocument>(query);

    osqlQuery.setFetchPlan(FETCH_PLAN);

    List<ODocument> resultList =
      connection.command(osqlQuery).setLimit(1).execute(parameters);

    if (Util.isNotEmpty(resultList))
    {
      result = resultList.get(0);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param converter
   * @param items
   * @param <T>
   *
   * @return
   */
  public static <T> List<ODocument> transformToDocuments(
          Converter<T> converter, List<T> items)
  {
    List<ODocument> docs = null;

    if (Util.isNotEmpty(items))
    {
      docs = Lists.transform(items, new ItemConverterFunction<T>(converter));
    }

    return docs;
  }

  /**
   * Method description
   *
   *
   * @param converter
   * @param docs
   * @param <T>
   *
   * @return
   */
  public static <T> List<T> transformToItems(Converter<T> converter,
          List<ODocument> docs)
  {
    List<T> items = null;

    if (Util.isNotEmpty(docs))
    {
      items = Lists.transform(docs,
                              new DocumentConverterFunction<T>(converter));
    }

    return items;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @param <T>
   *
   * @version        Enter version here..., 12/03/12
   * @author         Enter your name here...
   */
  private static class DocumentConverterFunction<T>
          implements Function<ODocument, T>
  {

    /**
     * Constructs ...
     *
     *
     * @param converter
     */
    public DocumentConverterFunction(Converter<T> converter)
    {
      this.converter = converter;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     *
     * @param doc
     *
     * @return
     */
    @Override
    public T apply(ODocument doc)
    {
      return converter.convert(doc);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private Converter<T> converter;
  }


  /**
   * Class description
   *
   *
   * @param <F>
   *
   * @version        Enter version here..., 12/03/12
   * @author         Enter your name here...
   */
  private static class ItemConverterFunction<F>
          implements Function<F, ODocument>
  {

    /**
     * Constructs ...
     *
     *
     * @param converter
     */
    public ItemConverterFunction(Converter<F> converter)
    {
      this.converter = converter;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param f
     *
     * @return
     */
    @Override
    public ODocument apply(F f)
    {
      return converter.convert(f);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private Converter<F> converter;
  }
}

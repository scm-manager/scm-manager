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



package sonia.scm.xml;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.GenericDAO;
import sonia.scm.ModelObject;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.store.Store;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <I>
 * @param <T>
 */
public abstract class AbstractXmlDAO<I extends ModelObject,
        T extends XmlDatabase<I>> implements GenericDAO<I>
{

  /** Field description */
  public static final String TYPE = "xml";

  /**
   * the logger for XmlGroupDAO
   */
  private static final Logger logger =
    LoggerFactory.getLogger(XmlGroupDAO.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param store
   */
  public AbstractXmlDAO(Store<T> store)
  {
    this.store = store;
    db = store.get();

    if (db == null)
    {
      db = createNewDatabase();
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param item
   *
   * @return
   */
  protected abstract I clone(I item);

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract T createNewDatabase();

  /**
   * Method description
   *
   *
   *
   * @param item
   */
  @Override
  public void add(I item)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("add item {} to xml backend", item.getId());
    }

    synchronized (store)
    {
      db.add(clone(item));
      storeDB();
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
  public boolean contains(I item)
  {
    return contains(item.getId());
  }

  /**
   * Method description
   *
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public boolean contains(String id)
  {
    return db.contains(id);
  }

  /**
   * Method description
   *
   *
   *
   * @param item
   */
  @Override
  public void delete(I item)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("delete item {} from xml backend", item.getId());
    }

    synchronized (store)
    {
      db.remove(item.getId());
      storeDB();
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
  public void modify(I item)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("modify xml backend item {}", item.getId());
    }

    synchronized (store)
    {
      db.remove(item.getId());
      db.add(clone(item));
      storeDB();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public I get(String id)
  {
    return db.get(id);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<I> getAll()
  {
    return db.values();
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
    return db.getCreationTime();
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
    return db.getLastModified();
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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  protected void storeDB()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("store xml database");
    }

    db.setLastModified(System.currentTimeMillis());
    store.set(db);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Store<T> store;

  /** Field description */
  protected T db;
}

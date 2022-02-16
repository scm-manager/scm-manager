/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.xml;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.GenericDAO;
import sonia.scm.ModelObject;
import sonia.scm.store.ConfigurationStore;

import java.util.Collection;

//~--- JDK imports ------------------------------------------------------------

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
    LoggerFactory.getLogger(AbstractXmlDAO.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param store
   */
  public AbstractXmlDAO(ConfigurationStore<T> store)
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
  @SuppressWarnings("unchecked")
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
  @SuppressWarnings("unchecked")
  public I get(String id)
  {
    return (I) db.get(id);
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
    // avoid concurrent modification exceptions
    return ImmutableList.copyOf(db.values());
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
  protected final ConfigurationStore<T> store;

  /** Field description */
  protected T db;
}

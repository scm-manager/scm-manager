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


import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.GenericDAO;
import sonia.scm.ModelObject;
import sonia.scm.store.ConfigurationStore;

import java.util.Collection;

public abstract class AbstractXmlDAO<I extends ModelObject,
      T extends XmlDatabase<I>> implements GenericDAO<I>
{

  public static final String TYPE = "xml";

 
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractXmlDAO.class);

  protected final ConfigurationStore<T> store;

  protected T db;

  public AbstractXmlDAO(ConfigurationStore<T> store)
  {
    this.store = store;
    db = store.get();

    if (db == null)
    {
      db = createNewDatabase();
    }
  }



  protected abstract I clone(I item);

  
  protected abstract T createNewDatabase();


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


  @Override
  public boolean contains(I item)
  {
    return contains(item.getId());
  }


  @Override
  public boolean contains(String id)
  {
    return db.contains(id);
  }


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



  @Override
  @SuppressWarnings("unchecked")
  public I get(String id)
  {
    return (I) db.get(id);
  }

  
  @Override
  public Collection<I> getAll()
  {
    // avoid concurrent modification exceptions
    return ImmutableList.copyOf(db.values());
  }

  
  @Override
  public Long getCreationTime()
  {
    return db.getCreationTime();
  }

  
  @Override
  public Long getLastModified()
  {
    return db.getLastModified();
  }

  
  @Override
  public String getType()
  {
    return TYPE;
  }


   protected void storeDB()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("store xml database");
    }

    db.setLastModified(System.currentTimeMillis());
    store.set(db);
  }

}

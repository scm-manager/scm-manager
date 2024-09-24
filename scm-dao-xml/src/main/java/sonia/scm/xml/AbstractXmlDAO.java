/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

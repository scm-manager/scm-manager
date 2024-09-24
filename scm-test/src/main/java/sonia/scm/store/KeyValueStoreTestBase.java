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

package sonia.scm.store;


import org.junit.Before;
import org.junit.Test;

import sonia.scm.AbstractTestBase;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;


public abstract class KeyValueStoreTestBase extends AbstractTestBase
{

  protected Repository repository = RepositoryTestData.createHeartOfGold();
  protected DataStore<StoreObject> store;
  protected DataStore<StoreObject> repoStore;
  protected String repoStoreName = "testRepoStore";
  protected String storeName = "testStore";

  
  protected abstract <STORE_OBJECT> DataStore<STORE_OBJECT> getDataStore(Class<STORE_OBJECT> type , Repository repository);
  protected abstract <STORE_OBJECT> DataStore<STORE_OBJECT> getDataStore(Class<STORE_OBJECT> type );



   @Before
  public void before()
  {
    store = getDataStore(StoreObject.class);
    repoStore = getDataStore(StoreObject.class, repository);
    store.clear();
    repoStore.clear();
  }

   @Test
  public void testClear()
  {
    testPutWithId();

    store.clear();
    assertNull(store.get("1"));
    assertNull(store.get("2"));

    assertTrue(store.getAll().isEmpty());
  }

   @Test
  public void testGet()
  {
    StoreObject other = store.get("1");

    assertNull(other);

    StoreObject obj = new StoreObject("test-1");

    store.put("1", obj);
    other = store.get("1");
    assertNotNull(other);
    assertEquals(obj, other);
  }

   @Test
  public void testGetAll()
  {
    StoreObject obj1 = new StoreObject("test-1");

    store.put("1", obj1);

    StoreObject obj2 = new StoreObject("test-2");

    store.put("2", obj2);

    Map<String, StoreObject> map = store.getAll();

    assertNotNull(map);

    assertFalse(map.isEmpty());
    assertEquals(2, map.size());

    assertEquals(obj1, map.get("1"));
    assertEquals(obj2, map.get("2"));

    assertNull(map.get("3"));
  }

   @Test
  public void testGetAllFromEmpty()
  {
    Map<String, StoreObject> map = store.getAll();

    assertNotNull(map);
    assertTrue(map.isEmpty());
  }

   @Test
  public void testGetFromEmpty()
  {
    StoreObject obj = store.get("test");

    assertNull(obj);
  }

   @Test
  public void testPutWithId()
  {
    StoreObject obj1 = new StoreObject("test-1");

    store.put("1", obj1);

    StoreObject obj2 = new StoreObject("test-2");

    store.put("2", obj2);

    assertEquals(obj1, store.get("1"));
    assertEquals(obj2, store.get("2"));
  }

   @Test
  public void testPutWithoutId()
  {
    StoreObject obj = new StoreObject("test-1");
    String id = store.put(obj);

    assertNotNull(id);

    assertEquals(obj, store.get(id));
  }

   @Test
  public void testRemove()
  {
    testPutWithId();

    store.remove("1");
    assertNull(store.get("1"));
    assertNotNull(store.get("2"));
    store.remove("2");
    assertNull(store.get("2"));
  }


}

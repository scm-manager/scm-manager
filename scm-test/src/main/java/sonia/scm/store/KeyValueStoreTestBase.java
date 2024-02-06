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

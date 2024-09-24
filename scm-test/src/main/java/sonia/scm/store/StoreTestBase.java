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


import org.junit.Test;
import sonia.scm.AbstractTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;



public abstract class StoreTestBase extends AbstractTestBase
{

  
  protected abstract ConfigurationStoreFactory createStoreFactory();

   @Test
  public void testGet()
  {
    ConfigurationStore<StoreObject> store = createStoreFactory().withType(StoreObject.class).withName("test").build();

    assertNotNull(store);

    StoreObject object = store.get();

    assertNull(object);
  }

   @Test
  public void testSet()
  {
    ConfigurationStore<StoreObject> store = createStoreFactory().withType(StoreObject.class).withName("test").build();

    assertNotNull(store);

    StoreObject obj = new StoreObject("asd");

    store.set(obj);

    StoreObject other = store.get();

    assertNotNull(other);
    assertEquals(obj, other);
  }
}

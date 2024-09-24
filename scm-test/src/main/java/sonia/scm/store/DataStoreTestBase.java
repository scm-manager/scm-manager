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
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public abstract class DataStoreTestBase extends KeyValueStoreTestBase
{


  protected abstract DataStoreFactory createDataStoreFactory();






  @Test
  public void shouldStoreRepositorySpecificData()
  {
    DataStoreFactory dataStoreFactory = createDataStoreFactory();
    StoreObject obj = new StoreObject("test-1");
    Repository repository = RepositoryTestData.createHeartOfGold();

    DataStore<StoreObject> store = dataStoreFactory
      .withType(StoreObject.class)
      .withName("test")
      .forRepository(repository)
      .build();

    String id = store.put(obj);

    assertNotNull(id);

    assertEquals(obj, store.get(id));
  }
}

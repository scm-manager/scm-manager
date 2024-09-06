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
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.security.UUIDKeyGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class JAXBDataStoreTest extends DataStoreTestBase {

  private final RepositoryReadOnlyChecker readOnlyChecker = mock(RepositoryReadOnlyChecker.class);

  @Override
  protected DataStoreFactory createDataStoreFactory()
  {
    return new JAXBDataStoreFactory(
      contextProvider,
      repositoryLocationResolver,
      new UUIDKeyGenerator(),
      readOnlyChecker,
      new DataFileCache(null, false),
      new StoreCacheConfigProvider(false)
    );
  }

  @Override
  protected <T> DataStore<T> getDataStore(Class<T> type, Repository repository) {
    return createDataStoreFactory()
      .withType(type)
      .withName("test")
      .forRepository(repository)
      .build();
  }

  @Override
  protected <T> DataStore<T> getDataStore(Class<T> type) {
    return createDataStoreFactory()
      .withType(type)
      .withName("test")
      .build();
  }

  @Test
  public void shouldStoreAndLoadInRepository()
  {
    repoStore.put("abc", new StoreObject("abc_value"));
    StoreObject storeObject = repoStore.get("abc");

    assertNotNull(storeObject);
    assertEquals("abc_value", storeObject.getValue());
  }

  @Test(expected = StoreReadOnlyException.class)
  public void shouldNotStoreForReadOnlyRepository()
  {
    when(readOnlyChecker.isReadOnly(repository.getId())).thenReturn(true);
    getDataStore(StoreObject.class, repository).put("abc", new StoreObject("abc_value"));
  }

  @Test
  public void testGetAllWithNonXmlFile() throws IOException {
    StoreObject obj1 = new StoreObject("test-1");

    store.put("1", obj1);

    new File(getTempDirectory(), "var/data/test/no-xml").createNewFile();

    Map<String, StoreObject> map = store.getAll();

    assertEquals(obj1, map.get("1"));
  }
}

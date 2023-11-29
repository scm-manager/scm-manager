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

/**
 *
 * @author Sebastian Sdorra
 */
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

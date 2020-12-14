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

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryArchivedCheck;
import sonia.scm.security.UUIDKeyGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Sebastian Sdorra
 */
public class JAXBDataStoreTest extends DataStoreTestBase {

  private final RepositoryArchivedCheck archivedCheck = mock(RepositoryArchivedCheck.class);

  @Override
  protected DataStoreFactory createDataStoreFactory()
  {
    return new JAXBDataStoreFactory(contextProvider, repositoryLocationResolver, new UUIDKeyGenerator(), archivedCheck);
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
    when(archivedCheck.isArchived(repository.getId())).thenReturn(true);
    getDataStore(StoreObject.class, repository).put("abc", new StoreObject("abc_value"));
  }
}

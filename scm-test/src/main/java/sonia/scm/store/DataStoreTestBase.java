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
import sonia.scm.repository.RepositoryTestData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class DataStoreTestBase extends KeyValueStoreTestBase
{

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract DataStoreFactory createDataStoreFactory();


  //~--- get methods ----------------------------------------------------------




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

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
import sonia.scm.AbstractTestBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

//~--- JDK imports ------------------------------------------------------------


/**
 *
 * @author Sebastian Sdorra
 */
public abstract class StoreTestBase extends AbstractTestBase
{

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract ConfigurationStoreFactory createStoreFactory();

  /**
   * Method description
   *
   */
  @Test
  public void testGet()
  {
    ConfigurationStore<StoreObject> store = createStoreFactory().withType(StoreObject.class).withName("test").build();

    assertNotNull(store);

    StoreObject object = store.get();

    assertNull(object);
  }

  /**
   * Method description
   *
   */
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

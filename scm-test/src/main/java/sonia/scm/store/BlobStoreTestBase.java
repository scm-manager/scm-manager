/**
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

import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.AbstractTestBase;
import sonia.scm.repository.RepositoryTestData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class BlobStoreTestBase extends AbstractTestBase
{

  protected abstract BlobStoreFactory createBlobStoreFactory();

  /**
   * Method description
   *
   */
  @Before
  public void createBlobStore()
  {
    store = createBlobStoreFactory()
      .withName("test")
      .forRepository(RepositoryTestData.createHeartOfGold())
      .build();
    store.clear();
  }

  /**
   * Method description
   *
   */
  @Test
  public void testClear()
  {
    store.create("1");
    store.create("2");
    store.create("3");

    assertNotNull(store.get("1"));
    assertNotNull(store.get("2"));
    assertNotNull(store.get("3"));

    store.clear();

    assertNull(store.get("1"));
    assertNull(store.get("2"));
    assertNull(store.get("3"));
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testContent() throws IOException
  {
    Blob blob = store.create();

    write(blob, "Hello");
    assertEquals("Hello", read(blob));

    blob = store.get(blob.getId());
    assertEquals("Hello", read(blob));

    write(blob, "Other Text");
    assertEquals("Other Text", read(blob));

    blob = store.get(blob.getId());
    assertEquals("Other Text", read(blob));
  }

  /**
   * Method description
   *
   */
  @Test(expected = EntryAlreadyExistsStoreException.class)
  public void testCreateAlreadyExistingEntry()
  {
    assertNotNull(store.create("1"));
    store.create("1");
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateWithId()
  {
    Blob blob = store.create("1");

    assertNotNull(blob);

    blob = store.get("1");
    assertNotNull(blob);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateWithoutId()
  {
    Blob blob = store.create();

    assertNotNull(blob);

    String id = blob.getId();

    assertNotNull(id);

    blob = store.get(id);
    assertNotNull(blob);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGet()
  {
    Blob blob = store.get("1");

    assertNull(blob);

    blob = store.create("1");
    assertNotNull(blob);

    blob = store.get("1");
    assertNotNull(blob);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetAll()
  {
    store.create("1");
    store.create("2");
    store.create("3");

    List<Blob> all = store.getAll();

    assertNotNull(all);
    assertFalse(all.isEmpty());
    assertEquals(3, all.size());

    boolean c1 = false;
    boolean c2 = false;
    boolean c3 = false;

    for (Blob b : all)
    {
      if ("1".equals(b.getId()))
      {
        c1 = true;
      }
      else if ("2".equals(b.getId()))
      {
        c2 = true;
      }
      else if ("3".equals(b.getId()))
      {
        c3 = true;
      }
    }

    assertTrue(c1);
    assertTrue(c2);
    assertTrue(c3);
  }

  /**
   * Method description
   *
   *
   * @param blob
   *
   * @return
   *
   * @throws IOException
   */
  private String read(Blob blob) throws IOException
  {
    InputStream input = blob.getInputStream();
    byte[] bytes = ByteStreams.toByteArray(input);

    input.close();

    return new String(bytes);
  }

  /**
   * Method description
   *
   *
   * @param blob
   * @param content
   *
   * @throws IOException
   */
  private void write(Blob blob, String content) throws IOException
  {
    OutputStream output = blob.getOutputStream();

    output.write(content.getBytes());
    output.close();
    blob.commit();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private BlobStore store;
}

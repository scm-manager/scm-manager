/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.ByteStreams;

import org.junit.Before;
import org.junit.Test;

import sonia.scm.AbstractTestBase;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class BlobStoreTestBase extends AbstractTestBase
{

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract BlobStoreFactory createBlobStoreFactory();

  /**
   * Method description
   *
   */
  @Before
  public void createBlobStore()
  {
    store = createBlobStoreFactory().getBlobStore("test");
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

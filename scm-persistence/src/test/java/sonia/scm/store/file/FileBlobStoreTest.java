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

package sonia.scm.store.file;

import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.AbstractTestBase;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.security.UUIDKeyGenerator;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.EntryAlreadyExistsStoreException;
import sonia.scm.store.StoreReadOnlyException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileBlobStoreTest extends AbstractTestBase
{

  private final Repository repository = RepositoryTestData.createHeartOfGold();
  private final RepositoryReadOnlyChecker readOnlyChecker = mock(RepositoryReadOnlyChecker.class);
  private BlobStore store;

  @BeforeEach
  void createBlobStore()
  {
    store = createBlobStoreFactory()
      .withName("test")
      .forRepository(repository)
      .build();
  }

  @Test
  void testClear()
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

  @Test
  void testContent() throws IOException
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

  @Test
  void testCreateAlreadyExistingEntry()
  {
    assertNotNull(store.create("1"));
    assertThrows(EntryAlreadyExistsStoreException.class, () -> store.create("1"));
  }

  @Test
  void testCreateWithId()
  {
    Blob blob = store.create("1");

    assertNotNull(blob);

    blob = store.get("1");
    assertNotNull(blob);
  }

  @Test
  void testCreateWithoutId()
  {
    Blob blob = store.create();

    assertNotNull(blob);

    String id = blob.getId();

    assertNotNull(id);

    blob = store.get(id);
    assertNotNull(blob);
  }

  @Test
  void testGet()
  {
    Blob blob = store.get("1");

    assertNull(blob);

    blob = store.create("1");
    assertNotNull(blob);

    blob = store.get("1");
    assertNotNull(blob);
  }

  @Test
  void testGetAll()
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

  @Nested
  class WithArchivedRepository {

    @BeforeEach
    void setRepositoryArchived() {
      store.create("1"); // store for test must not be empty
      when(readOnlyChecker.isReadOnly(repository.getId())).thenReturn(true);
      createBlobStore();
    }

    @Test
    void shouldNotClear() {
      assertThrows(StoreReadOnlyException.class, () -> store.clear());
    }

    @Test
    void shouldNotRemove() {
      assertThrows(StoreReadOnlyException.class, () -> store.remove("1"));
    }
  }

  private String read(Blob blob) throws IOException
  {
    InputStream input = blob.getInputStream();
    byte[] bytes = ByteStreams.toByteArray(input);

    input.close();

    return new String(bytes);
  }

  private void write(Blob blob, String content) throws IOException
  {
    OutputStream output = blob.getOutputStream();

    output.write(content.getBytes());
    output.close();
    blob.commit();
  }

  protected BlobStoreFactory createBlobStoreFactory()
  {
    return new FileBlobStoreFactory(contextProvider, repositoryLocationResolver, new UUIDKeyGenerator(), readOnlyChecker);
  }
}

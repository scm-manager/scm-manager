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

package store;

import org.junit.jupiter.api.Test;
import sonia.scm.store.Blob;
import sonia.scm.store.InMemoryBlobStore;

import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryBlobStoreTest {

  @Test
  void shouldStoreToBlob() throws IOException {
    String content = "SCM-Manager";
    InMemoryBlobStore inMemoryBlobStore = new InMemoryBlobStore();
    Blob blob = inMemoryBlobStore.create();
    OutputStream os = blob.getOutputStream();
    os.write(content.getBytes());
    os.flush();
    os.close();

    byte[] result = new byte[11];
    blob.getInputStream().read(result);
    assertThat(new String(result)).isEqualTo(content);
  }

  @Test
  void shouldGetBlobById() {
    InMemoryBlobStore inMemoryBlobStore = new InMemoryBlobStore();
    Blob first = inMemoryBlobStore.create("1");
    inMemoryBlobStore.create("2");
    inMemoryBlobStore.create("3");

    assertThat(inMemoryBlobStore.get("1")).isEqualTo(first);
  }

  @Test
  void shouldGetAllBlobs() {
    InMemoryBlobStore inMemoryBlobStore = new InMemoryBlobStore();
    Blob first = inMemoryBlobStore.create("1");
    Blob second = inMemoryBlobStore.create("2");
    Blob third = inMemoryBlobStore.create("3");

    assertThat(inMemoryBlobStore.getAll()).contains(first, second, third);
  }

  @Test
  void shouldRemoveBlobById() {
    InMemoryBlobStore inMemoryBlobStore = new InMemoryBlobStore();
    Blob blob = inMemoryBlobStore.create("1");

    assertThat(inMemoryBlobStore.get("1")).isEqualTo(blob);

    inMemoryBlobStore.remove("1");
    assertThat(inMemoryBlobStore.getAll()).isEmpty();
  }

  @Test
  void shouldRemoveBlob() {
    InMemoryBlobStore inMemoryBlobStore = new InMemoryBlobStore();
    Blob blob = inMemoryBlobStore.create("1");

    assertThat(inMemoryBlobStore.get("1")).isEqualTo(blob);

    inMemoryBlobStore.remove(blob);
    assertThat(inMemoryBlobStore.getAll()).isEmpty();
  }

  @Test
  void shouldClearBlobs() {
    InMemoryBlobStore inMemoryBlobStore = new InMemoryBlobStore();
    inMemoryBlobStore.create();
    inMemoryBlobStore.create();

    assertThat(inMemoryBlobStore.getAll()).hasSize(2);

    inMemoryBlobStore.clear();

    assertThat(inMemoryBlobStore.getAll()).isEmpty();
  }
}

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

import org.junit.jupiter.api.Test;

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

    byte[] result = new byte[1024];
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

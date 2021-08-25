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

import com.google.common.collect.ImmutableList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryBlobStore implements BlobStore {

  private final List<Blob> blobs = new ArrayList<>();

  @Override
  public Blob create() {
    InMemoryBlob blob = new InMemoryBlob(UUID.randomUUID().toString());
    blobs.add(blob);
    return blob;
  }

  @Override
  public Blob create(String id) {
    InMemoryBlob blob = new InMemoryBlob(id);
    blobs.add(blob);
    return blob;
  }

  @Override
  public void remove(Blob blob) {
    blobs.remove(blob);
  }

  @Override
  public List<Blob> getAll() {
    return ImmutableList.copyOf(blobs);
  }

  @Override
  public void clear() {
    blobs.clear();
  }

  @Override
  public void remove(String id) {
    blobs.stream()
      .filter(b -> b.getId().equals(id))
      .findFirst()
      .ifPresent(blobs::remove);
  }

  @Override
  public Blob get(String id) {
    return blobs.stream()
      .filter(b -> b.getId().equals(id))
      .findFirst()
      .orElse(null);
  }

  private static class InMemoryBlob implements Blob {

    private final String id;
    private byte[] bytes = new byte[0];

    private InMemoryBlob(String id) {
      this.id = id;
    }

    @Override
    public void commit() {
      //Do nothing
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
      return new ByteArrayInputStream(bytes);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return new InMemoryBlobByteArrayOutputStream();
    }

    @Override
    public long getSize() {
      return bytes.length;
    }

    private class InMemoryBlobByteArrayOutputStream extends ByteArrayOutputStream {

      @Override
      public void close() throws IOException {
        bytes = super.toByteArray();
        super.close();
      }
    }
  }
}

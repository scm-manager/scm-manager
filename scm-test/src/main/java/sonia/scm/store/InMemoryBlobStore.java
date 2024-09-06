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

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


import sonia.scm.store.Blob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File base implementation of {@link Blob}.
 *
 */
final class FileBlob implements Blob {

  private final String id;
  private final File file;

  FileBlob(String id, File file) {
    this.id = id;
    this.file = file;
  }

  @Override
  public void commit() throws IOException {

    // nothing to do
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public InputStream getInputStream() throws FileNotFoundException {
    return new FileInputStream(file);
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return new FileOutputStream(file);
  }

  @Override
  public long getSize() {
    if (this.file.isFile()) {
      return this.file.length();
    } else {
      //to sum up all other cases, in which we cannot determine a size
      return -1;
    }
  }
}

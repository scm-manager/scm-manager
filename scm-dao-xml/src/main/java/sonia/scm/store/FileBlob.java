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
public final class FileBlob implements Blob {

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

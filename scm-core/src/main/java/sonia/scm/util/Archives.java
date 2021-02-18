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

package sonia.scm.util;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class Archives {

  private Archives() {
  }

  /**
   * Creates a tar output stream that is backed by the given output stream.
   * @param dest The stream the tar should be written to.
   */
  public static TarArchiveOutputStream writeTarStream(OutputStream dest) {
    BufferedOutputStream bos = new BufferedOutputStream(dest);
    TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(bos);
    tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
    tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
    return tarArchiveOutputStream;
  }

  /**
   * Creates a tar input stream that takes its bytes from the given input stream.
   * @param source The stream the tar should be extracted from.
   */
  public static TarArchiveInputStream readTarStream(InputStream source) {
    return new TarArchiveInputStream(source);
  }
}

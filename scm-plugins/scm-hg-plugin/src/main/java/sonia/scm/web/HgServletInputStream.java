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

package sonia.scm.web;

import com.google.common.base.Preconditions;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * HgServletInputStream is a wrapper around the original {@link ServletInputStream} and provides some extra
 * functionality to support the mercurial client.
 */
public class HgServletInputStream extends ServletInputStream {

  private final ServletInputStream original;
  private ByteArrayInputStream captured;

  HgServletInputStream(ServletInputStream original) {
    this.original = original;
  }

  /**
   * Reads the given amount of bytes from the stream and captures them, if the {@link #read()} methods is called the
   * captured bytes are returned before the rest of the stream.
   *
   * @param size amount of bytes to read
   *
   * @return byte array
   *
   * @throws IOException if the method is called twice
   */
  public byte[] readAndCapture(int size) throws IOException {
    Preconditions.checkState(captured == null, "readAndCapture can only be called once per request");

    // TODO should we enforce a limit? to prevent OOM?
    byte[] bytes = new byte[size];
    original.read(bytes);
    captured = new ByteArrayInputStream(bytes);

    return bytes;
  }

  @Override
  public int read() throws IOException {
    if (captured != null && captured.available() > 0) {
       return captured.read();
    }
    return original.read();
  }

  @Override
  public void close() throws IOException {
    original.close();
  }

  @Override
  public boolean isFinished() {
    return original.isFinished();
  }

  @Override
  public boolean isReady() {
    return original.isReady();
  }

  @Override
  public void setReadListener(ReadListener readListener) {
    original.setReadListener(readListener);
  }
}

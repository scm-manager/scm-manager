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

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HgServletInputStreamTest {

  @Test
  public void testReadAndCapture() throws IOException {
    SampleServletInputStream original = new SampleServletInputStream("trillian.mcmillian@hitchhiker.com");
    HgServletInputStream hgServletInputStream = new HgServletInputStream(original);

    byte[] prefix = hgServletInputStream.readAndCapture(8);
    assertEquals("trillian", new String(prefix, Charsets.US_ASCII));

    byte[] wholeBytes = ByteStreams.toByteArray(hgServletInputStream);
    assertEquals("trillian.mcmillian@hitchhiker.com", new String(wholeBytes, Charsets.US_ASCII));
  }

  @Test(expected = IllegalStateException.class)
  public void testReadAndCaptureCalledTwice() throws IOException {
    SampleServletInputStream original = new SampleServletInputStream("trillian.mcmillian@hitchhiker.com");
    HgServletInputStream hgServletInputStream = new HgServletInputStream(original);

    hgServletInputStream.readAndCapture(1);
    hgServletInputStream.readAndCapture(1);
  }

  private static class SampleServletInputStream extends ServletInputStream {

    private ByteArrayInputStream input;

    private SampleServletInputStream(String data) {
      input = new ByteArrayInputStream(data.getBytes());
    }

    @Override
    public int read() {
      return input.read();
    }

    @Override
    public boolean isFinished() {
      return false;
    }

    @Override
    public boolean isReady() {
      return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
    }
  }

}

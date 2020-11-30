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

package sonia.scm.repository.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Sockets {

  private static final Logger LOG = LoggerFactory.getLogger(Sockets.class);

  private static final int READ_LIMIT = 8192;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private Sockets() {
  }

  static void send(OutputStream out, Object object) throws IOException {
    byte[] bytes = objectMapper.writeValueAsBytes(object);
    LOG.trace("send message length of {} to socket", bytes.length);

    DataOutputStream dataOutputStream = new DataOutputStream(out);
    dataOutputStream.writeInt(bytes.length);

    LOG.trace("send message to socket");
    dataOutputStream.write(bytes);

    LOG.trace("flush socket");
    out.flush();
  }

  static <T> T receive(InputStream in, Class<T> type) throws IOException {
    LOG.trace("read {} from socket", type);

    DataInputStream dataInputStream = new DataInputStream(in);

    int length = dataInputStream.readInt();
    LOG.trace("read message length of {} from socket", length);
    if (length > READ_LIMIT) {
      String message = String.format("received length of %d, which exceeds the limit of %d", length, READ_LIMIT);
      throw new IOException(message);
    }

    byte[] data = new byte[length];
    dataInputStream.readFully(data);

    LOG.trace("convert message to {}", type);
    return objectMapper.readValue(data, type);
  }

}

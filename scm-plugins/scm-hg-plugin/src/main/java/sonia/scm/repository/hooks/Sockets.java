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

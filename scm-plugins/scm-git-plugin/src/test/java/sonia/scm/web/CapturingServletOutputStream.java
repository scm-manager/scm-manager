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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CapturingServletOutputStream extends ServletOutputStream {

  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

  @Override
  public void write(int b) throws IOException {
    baos.write(b);
  }

  @Override
  public void close() throws IOException {
    baos.close();
  }

  @Override
  public String toString() {
    return baos.toString();
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {
  }

  public JsonNode getContentAsJson() {
    try {
      return new ObjectMapper().readTree(toString());
    } catch (JsonProcessingException e) {
      throw new RuntimeException("could not unmarshal json content", e);
    }
  }
}

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
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.AsyncOutputStream;
import org.jboss.resteasy.spi.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class JsonMockHttpResponse implements HttpResponse {

  private final MockHttpResponse delegate = new MockHttpResponse();

  @Override
  public int getStatus() {
    return delegate.getStatus();
  }

  @Override
  public void setStatus(int status) {
    delegate.setStatus(status);
  }

  @Override
  public MultivaluedMap<String, Object> getOutputHeaders() {
    return delegate.getOutputHeaders();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return delegate.getOutputStream();
  }

  @Override
  public void setOutputStream(OutputStream os) {
    delegate.setOutputStream(os);
  }

  public byte[] getOutput() {
    return delegate.getOutput();
  }

  public String getContentAsString() throws UnsupportedEncodingException {
    return delegate.getContentAsString();
  }

  @Override
  public void addNewCookie(NewCookie cookie) {
    delegate.addNewCookie(cookie);
  }

  @Override
  public void sendError(int status) throws IOException {
    delegate.sendError(status);
  }

  @Override
  public void sendError(int status, String message) throws IOException {
    delegate.sendError(status, message);
  }

  public List<NewCookie> getNewCookies() {
    return delegate.getNewCookies();
  }

  public String getErrorMessage() {
    return delegate.getErrorMessage();
  }

  public boolean isErrorSent() {
    return delegate.isErrorSent();
  }

  @Override
  public boolean isCommitted() {
    return delegate.isCommitted();
  }

  @Override
  public void reset() {
    delegate.reset();
  }

  @Override
  public void flushBuffer() throws IOException {
    delegate.flushBuffer();
  }

  @Override
  public AsyncOutputStream getAsyncOutputStream() throws IOException {
    return delegate.getAsyncOutputStream();
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

  @Override
  public void setSuppressExceptionDuringChunkedTransfer(boolean suppressExceptionDuringChunkedTransfer) {
    delegate.setSuppressExceptionDuringChunkedTransfer(suppressExceptionDuringChunkedTransfer);
  }

  @Override
  public boolean suppressExceptionDuringChunkedTransfer() {
    return delegate.suppressExceptionDuringChunkedTransfer();
  }

  public <T> T getContentAs(Class<T> clazz) {
    try {
      return new ObjectMapper().readValue(getContentAsString(), clazz);
    } catch (JsonProcessingException | UnsupportedEncodingException e) {
      throw new RuntimeException("could not unmarshal content for class " + clazz, e);
    }
  }

  public JsonNode getContentAsJson() {
    try {
      return new ObjectMapper().readTree(getContentAsString());
    } catch (JsonProcessingException | UnsupportedEncodingException e) {
      throw new RuntimeException("could not unmarshal json content", e);
    }
  }
}

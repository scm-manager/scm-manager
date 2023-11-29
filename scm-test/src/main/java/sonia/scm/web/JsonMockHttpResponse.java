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

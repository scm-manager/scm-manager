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

package sonia.scm.net.ahc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * The form builder is able to add form parameters to a request.
 *
 * @since 1.46
 */
public class FormContentBuilder {

  private final AdvancedHttpRequestWithBody request;
  private final List<StringFormEntry> stringEntries = new ArrayList<>();
  private final List<FileFormEntry> fileEntries = new ArrayList<>();
  private final Supplier<String> boundaryFactory;

  public FormContentBuilder(AdvancedHttpRequestWithBody request) {
    this(request, () -> "------------------------" + System.currentTimeMillis());
  }

  @VisibleForTesting
  FormContentBuilder(AdvancedHttpRequestWithBody request, Supplier<String> boundaryFactory) {
    this.request = request;
    this.boundaryFactory = boundaryFactory;
  }

  /**
   * Adds a form parameter.
   *
   * @param name parameter name
   * @param values parameter values
   *
   * @return {@code this}
   */
  public FormContentBuilder fields(String name, Iterable<? extends Object> values) {
    for (Object v : values) {
      append(name, v);
    }
    return this;
  }

  /**
   * Adds a formular parameter.
   *
   * @param name parameter name
   * @param values parameter values
   *
   * @return {@code this}
   */
  public FormContentBuilder field(String name, Object... values) {
    for (Object v : values) {
      append(name, v);
    }

    return this;
  }

  private void append(String name, Object value) {
    if (!Strings.isNullOrEmpty(name)) {
      stringEntries.add(new StringFormEntry(name, value != null ? value.toString() : ""));
    }
  }

  /**
   * Upload a file as part of the form request.
   * Whenever a file is part of the request,
   * the request is sent as multipart form request instead of an url encoded form.
   *
   * @param name parameter name
   * @param filename name of the file
   * @param content input stream of the file content
   *
   * @return {@code this}
   * @since 2.27.0
   */
  public FormContentBuilder file(String name, String filename, InputStream content) {
    fileEntries.add(new FileFormEntry(name, filename, content));
    return this;
  }

  /**
   * Build the form content and append it to the request.
   *
   * @return request instance
   */
  public AdvancedHttpRequestWithBody build() {
    if (fileEntries.isEmpty()) {
      request.contentType("application/x-www-form-urlencoded");
      request.stringContent(urlEncoded());
    } else {
      request.content(new MultipartFormContent(boundaryFactory.get()));
    }
    return request;
  }

  private String urlEncoded() {
    StringBuilder builder = new StringBuilder();
    Iterator<StringFormEntry> it = stringEntries.iterator();
    while (it.hasNext()) {
      StringFormEntry e = it.next();
      builder.append(HttpUtil.encode(e.name)).append("=").append(HttpUtil.encode(e.value));
      if (it.hasNext()) {
        builder.append("&");
      }
    }
    return builder.toString();
  }

  @RequiredArgsConstructor
  private static class StringFormEntry {
    private final String name;
    private final String value;
  }

  @RequiredArgsConstructor
  private static class FileFormEntry  {
    private final String name;
    private final String filename;
    private final InputStream stream;
  }

  class MultipartFormContent implements Content {

    private final String boundary;

    private MultipartFormContent(String boundary) {
      this.boundary = boundary;
    }

    @Override
    public void prepare(AdvancedHttpRequestWithBody request) throws IOException {
      request.contentType("multipart/form-data; boundary=" + boundary);
    }

    @Override
    public void process(OutputStream output) throws IOException {
      PrintWriter writer = new PrintWriter(output);

      writeBoundary(writer);
      for (StringFormEntry entry : stringEntries) {
        field(writer, entry);
      }

      for (FileFormEntry entry : fileEntries) {
        file(output, writer, entry);
      }

      writer.flush();
    }

    private void field(PrintWriter writer, StringFormEntry entry) {
      writeContentDisposition(writer, entry.name);
      writer.println();
      writer.println(entry.value);
      writeBoundary(writer);
    }

    private void file(OutputStream output, PrintWriter writer, FileFormEntry entry) throws IOException {
      writeContentDisposition(writer, entry.name, entry.filename);
      writer.println("Content-Transfer-Encoding: binary");
      writer.println();

      writer.flush();
      ByteStreams.copy(entry.stream, output);

      writer.println();
      writeBoundary(writer);
    }

    private void writeBoundary(PrintWriter writer) {
      writer.append("--").println(boundary);
    }

    private void writeContentDisposition(PrintWriter writer, String name) {
      writeContentDisposition(writer, name, null);
    }

    private void writeContentDisposition(PrintWriter writer, String name, String filename) {
      writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"");
      if (filename != null) {
        writer.append("; filename=\"").append(filename).append("\"");
      }
      writer.println();
    }
  }
}

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

package sonia.scm.cli;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class JsonStreamingCliContext implements CliContext, AutoCloseable {

  private final InputStream stdin;
  private final PrintStream stdout;
  private final PrintStream stderr;

  public JsonStreamingCliContext(InputStream stdin, OutputStream output) {
    this.stdin = stdin;
    try {
      this.stdout = new PrintStream(new StreamingOutput(output, "out"), true);
      this.stderr = new PrintStream(new StreamingOutput(output, "err"), true);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public PrintStream getStdout() {
    return stdout;
  }

  @Override
  public PrintStream getStderr() {
    return stderr;
  }

  @Override
  public InputStream getStdin() {
    return stdin;
  }

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void close() {
    stdout.close();
    stderr.close();
    try {
      stdin.close();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static class StreamingOutput extends OutputStream {

    private final JsonGenerator jsonGenerator;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final OutputStream underlyingOutputStream;
    private final String name;

    public StreamingOutput(OutputStream underlyingOutputStream, String name) throws IOException {
      this.jsonGenerator = mapper.createGenerator(underlyingOutputStream).setPrettyPrinter(new MinimalPrettyPrinter(""));
      this.underlyingOutputStream = underlyingOutputStream;
      this.name = name;
    }

    @Override
    public void write(int i) throws IOException {
      baos.write(i);
    }

    @Override
    public void write(byte[] b, int off, int len) {
      baos.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      String content = baos.toString();
      if (content.length() > 0) {
        baos.reset();
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(name, content);
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        underlyingOutputStream.write(10);
      }
    }
  }
}

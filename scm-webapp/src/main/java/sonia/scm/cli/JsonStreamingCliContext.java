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
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;

public class JsonStreamingCliContext implements CliContext, AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(JsonStreamingCliContext.class);

  private final Locale locale;
  private final Client client;
  private final InputStream stdin;
  private final PrintWriter stdout;
  private final PrintWriter stderr;
  private final JsonGenerator jsonGenerator;

  @SuppressWarnings("java:S2095") // generator is closed in the close method
  public JsonStreamingCliContext(Locale locale, Client client, InputStream stdin, OutputStream output) throws IOException {
    this.locale = locale;
    this.client = client;
    this.stdin = stdin;
    this.jsonGenerator = mapper.createGenerator(output).setPrettyPrinter(new MinimalPrettyPrinter(""));
    jsonGenerator.writeStartArray();

    StringingOutputWriter out = new StringingOutputWriter(jsonGenerator, "out");
    StringingOutputWriter err = new StringingOutputWriter(jsonGenerator, "err");
    out.setOther(err);
    err.setOther(out);

    this.stdout = new PrintWriter(out);
    this.stderr = new PrintWriter(err);
  }

  @Override
  public PrintWriter getStdout() {
    return stdout;
  }

  @Override
  public PrintWriter getStderr() {
    return stderr;
  }

  @Override
  public InputStream getStdin() {
    return stdin;
  }

  public void writeExit(int exitcode) throws IOException {
    stdout.flush();
    stderr.flush();
    jsonGenerator.writeStartObject();
    jsonGenerator.writeNumberField("exit", exitcode);
    jsonGenerator.writeEndObject();
  }

  @Override
  public void exit(int exitCode) {
    throw new CliExitException(exitCode);
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public Client getClient() {
    return client;
  }

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void close() {
    try {
      stdout.close();
      stderr.close();
      jsonGenerator.writeEndArray();
      jsonGenerator.close();
    } catch (IOException e) {
      LOG.error("Could not close cli output streams", e);
    }
    try {
      stdin.close();
    } catch (IOException e) {
      LOG.error("Could not close cli input stream", e);
    }
  }

  public static class StringingOutputWriter extends Writer {

    private final JsonGenerator jsonGenerator;
    private final String name;

    private StringBuilder buffer = new StringBuilder();
    private StringingOutputWriter other;

    private StringingOutputWriter(JsonGenerator jsonGenerator, String name) {
      this.jsonGenerator = jsonGenerator;
      this.name = name;
    }

    public void setOther(StringingOutputWriter other) {
      this.other = other;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      other.flush();
      buffer.append(Arrays.copyOfRange(cbuf, off, len));
    }

    @Override
    public void flush() throws IOException {
      String content = buffer.toString();
      if (content.length() > 0) {
        buffer = new StringBuilder();
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField(name, content);
        jsonGenerator.writeEndObject();
      }
    }

    @Override
    public void close() throws IOException {
      this.flush();
    }
  }
}

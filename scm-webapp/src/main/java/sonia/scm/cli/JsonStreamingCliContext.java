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
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class JsonStreamingCliContext implements CliContext, AutoCloseable {

  private final InputStream stdin;
  private final PrintWriter stdout;
  private final PrintWriter stderr;
  private final JsonGenerator jsonGenerator;
  private CommandLine commandLine;

  public JsonStreamingCliContext(InputStream stdin, OutputStream output) {
    this.stdin = stdin;
    try {
      this.jsonGenerator = mapper.createGenerator(output).setPrettyPrinter(new MinimalPrettyPrinter(""));
      jsonGenerator.writeStartArray();
      this.stdout = new PrintWriter(new StreamingOutput(jsonGenerator, "out"), true);
      this.stderr = new PrintWriter(new StreamingOutput(jsonGenerator, "err"), true);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
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

  @Override
  public void exit(int exitcode) {
    try {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeNumberField("exit", exitcode);
      jsonGenerator.writeEndObject();
    } catch (IOException e) {
      //TODO Handle
      e.printStackTrace();
    }
  }

  @Override
  public CommandLine getCommandLine() {
    return commandLine;
  }

  public void setCommandLine(CommandLine commandLine) {
    this.commandLine = commandLine;
  }

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void close() {
    try {
      jsonGenerator.writeEndArray();
      jsonGenerator.close();
      stdout.close();
      stderr.close();
      stdin.close();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static class StreamingOutput extends OutputStream {

    private final JsonGenerator jsonGenerator;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final String name;

    public StreamingOutput(JsonGenerator jsonGenerator, String name) {
      this.jsonGenerator = jsonGenerator;
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
      }
    }
  }
}

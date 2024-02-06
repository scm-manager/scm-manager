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
    
package sonia.scm.io;


import sonia.scm.util.IOUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractResourceProcessor implements ResourceProcessor
{
  private Map<String, String> variableMap = new HashMap<>();

  protected abstract void process(Map<String, String> variableMap,
                                  BufferedReader reader, BufferedWriter writer)
          throws IOException;

  @Override
  public void addVariable(String key, String value)
  {
    variableMap.put(key, value);
  }

  @Override
  public void process(InputStream input, OutputStream output) throws IOException
  {
    process(new InputStreamReader(input), new OutputStreamWriter(output));
  }

  @Override
  public void process(File input, File output) throws IOException
  {
    Reader reader = null;
    Writer writer = null;

    try
    {
      reader = new FileReader(input);
      writer = new FileWriter(output);
      process(reader, writer);
    }
    finally
    {
      IOUtil.close(reader);
      IOUtil.close(writer);
    }
  }

  @Override
  public void process(Reader reader, Writer writer) throws IOException
  {
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;

    if (reader instanceof BufferedReader)
    {
      bufferedReader = (BufferedReader) reader;
    }
    else
    {
      bufferedReader = new BufferedReader(reader);
    }

    if (writer instanceof BufferedWriter)
    {
      bufferedWriter = (BufferedWriter) writer;
    }
    else
    {
      bufferedWriter = new BufferedWriter(writer);
    }

    process(variableMap, bufferedReader, bufferedWriter);
  }

  @Override
  public void removeVariable(String key)
  {
    variableMap.remove(key);
  }

}

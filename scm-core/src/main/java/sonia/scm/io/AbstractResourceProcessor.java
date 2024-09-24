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

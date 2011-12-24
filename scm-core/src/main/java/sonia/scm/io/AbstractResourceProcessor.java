/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

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

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractResourceProcessor implements ResourceProcessor
{

  /**
   * Method description
   *
   *
   * @param variableMap
   * @param reader
   * @param writer
   *
   * @throws IOException
   */
  protected abstract void process(Map<String, String> variableMap,
                                  BufferedReader reader, BufferedWriter writer)
          throws IOException;

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  @Override
  public void addVariable(String key, String value)
  {
    variableMap.put(key, value);
  }

  /**
   * Method description
   *
   *
   * @param input
   * @param output
   *
   * @throws IOException
   */
  @Override
  public void process(InputStream input, OutputStream output) throws IOException
  {
    process(new InputStreamReader(input), new OutputStreamWriter(output));
  }

  /**
   * Method description
   *
   *
   * @param input
   * @param output
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param reader
   * @param writer
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param key
   */
  @Override
  public void removeVariable(String key)
  {
    variableMap.remove(key);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, String> variableMap = new HashMap<String, String>();
}

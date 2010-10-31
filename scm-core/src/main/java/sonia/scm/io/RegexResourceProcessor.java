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
import java.io.IOException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 */
public class RegexResourceProcessor extends AbstractResourceProcessor
{

  /** Field description */
  public static final Pattern PATTERN = Pattern.compile("\\$\\{([^\\$]+)\\}");

  //~--- methods --------------------------------------------------------------

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
  @Override
  protected void process(Map<String, String> variableMap,
                         BufferedReader reader, BufferedWriter writer)
          throws IOException
  {
    try
    {
      String line = reader.readLine();

      while (line != null)
      {
        line = parseLine(variableMap, line);
        writer.write(line);
        line = reader.readLine();

        if (line != null)
        {
          writer.newLine();
        }
      }
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
   * @param variableMap
   * @param line
   *
   * @return
   */
  private String parseLine(Map<String, String> variableMap, String line)
  {
    Matcher m = PATTERN.matcher(line);

    while (m.find())
    {
      String key = m.group(1);
      String value = variableMap.get(key);

      if (value != null)
      {
        line = m.replaceAll(value);
      }

      m = PATTERN.matcher(line);
    }

    return line;
  }
}

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
import java.io.IOException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexResourceProcessor extends AbstractResourceProcessor
{

  public static final Pattern PATTERN = Pattern.compile("\\$\\{([^\\$]+)\\}");


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

  private String parseLine(Map<String, String> variableMap, String line)
  {
    StringBuffer result = null;
    Matcher m = PATTERN.matcher(line);

    while (m.find())
    {
      String key = m.group(1);
      String value = variableMap.get(key);

      if (value != null)
      {
        if (result == null)
        {
          result = new StringBuffer();
        }

        m.appendReplacement(result, Matcher.quoteReplacement(value));
      }
    }

    if (result != null)
    {
      m.appendTail(result);
      line = result.toString();
    }

    return line;
  }
}

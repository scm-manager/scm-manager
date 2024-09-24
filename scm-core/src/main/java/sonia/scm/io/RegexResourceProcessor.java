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

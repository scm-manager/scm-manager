/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

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
      Util.close(reader);
      Util.close(writer);
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 */
public class INIConfigurationReader extends AbstractReader<INIConfiguration>
{

  /** Field description */
  private static final Pattern sectionPattern =
    Pattern.compile("\\[([^\\]]+)\\]");

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param input
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public INIConfiguration read(InputStream input) throws IOException
  {
    INIConfiguration configuration = new INIConfiguration();

    try
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(input));
      INISection section = null;
      String line = reader.readLine();

      while (line != null)
      {
        line = line.trim();

        Matcher sectionMatcher = sectionPattern.matcher(line);

        if (sectionMatcher.matches())
        {
          String name = sectionMatcher.group(1);

          if (section != null)
          {
            configuration.addSection(section);
          }

          section = new INISection(name);
        }
        else if ((section != null) &&!line.startsWith(";")
                 &&!line.startsWith("#"))
        {
          int index = line.indexOf("=");

          if (index > 0)
          {
            String key = line.substring(0, index).trim();
            String value = line.substring(index + 1, line.length()).trim();

            section.setParameter(key, value);
          }
        }

        line = reader.readLine();
      }

      if (section != null)
      {
        configuration.addSection(section);
      }
    }
    finally
    {
      IOUtil.close(input);
    }

    return configuration;
  }
}

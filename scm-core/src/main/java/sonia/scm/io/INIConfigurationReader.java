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

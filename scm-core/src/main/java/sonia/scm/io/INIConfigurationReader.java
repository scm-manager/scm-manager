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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read configuration in ini format from files and streams.
 */
public class INIConfigurationReader extends AbstractReader<INIConfiguration> {

  private static final Pattern sectionPattern = Pattern.compile("\\[([^]]+)]");

  @Override
  public INIConfiguration read(InputStream input) throws IOException {
    INIConfiguration configuration = new INIConfiguration();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

      INISection section = null;
      String line = reader.readLine();

      while (line != null) {
        line = line.trim();

        Matcher sectionMatcher = sectionPattern.matcher(line);

        if (sectionMatcher.matches()) {
          String name = sectionMatcher.group(1);

          if (section != null) {
            configuration.addSection(section);
          }

          section = new INISection(name);
        } else if ((section != null) && !line.startsWith(";") && !line.startsWith("#")) {
          int index = line.indexOf('=');

          if (index > 0) {
            String key = line.substring(0, index).trim();
            String value = line.substring(index + 1).trim();

            section.setParameter(key, value);
          }
        }

        line = reader.readLine();
      }

      if (section != null) {
        configuration.addSection(section);
      }
    }

    return configuration;
  }
}

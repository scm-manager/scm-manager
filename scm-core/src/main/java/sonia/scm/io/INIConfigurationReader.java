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

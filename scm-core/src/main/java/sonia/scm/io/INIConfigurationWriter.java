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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Write configurations in ini format to file and streams.
 */
public class INIConfigurationWriter extends AbstractWriter<INIConfiguration> {

  @Override
  public void write(INIConfiguration object, OutputStream output) throws IOException {
    try (PrintWriter writer = new PrintWriter(output)) {
      for (INISection section : object.getSections()) {
        writer.println(section.toString());
      }
    }
  }
}

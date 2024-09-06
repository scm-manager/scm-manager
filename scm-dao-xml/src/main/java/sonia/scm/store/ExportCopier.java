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

package sonia.scm.store;

import sonia.scm.repository.api.ExportFailedException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static sonia.scm.ContextEntry.ContextBuilder.noContext;

final class ExportCopier {

  private ExportCopier() {
  }

  static void putFileContentIntoStream(Exporter exporter, Path file) {
    try (OutputStream stream = exporter.put(file.getFileName().toString(), Files.size(file))) {
      Files.copy(file, stream);
    } catch (IOException e) {
      throw new ExportFailedException(
        noContext(),
        "Could not copy file to export stream: " + file,
        e
      );
    }
  }
}

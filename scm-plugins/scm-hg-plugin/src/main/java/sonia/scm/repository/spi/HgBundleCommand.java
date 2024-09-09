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

package sonia.scm.repository.spi;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import sonia.scm.ContextEntry;
import sonia.scm.repository.api.BundleResponse;
import sonia.scm.repository.api.ExportFailedException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static sonia.scm.util.Archives.addPathToTar;

public class HgBundleCommand implements BundleCommand {

  private static final String TAR_ARCHIVE = "tar";
  private final HgCommandContext context;

  @Inject
  public HgBundleCommand(@Assisted HgCommandContext context) {
    this.context = context;
  }

  @Override
  public BundleResponse bundle(BundleCommandRequest request) throws IOException {
    Path repoDir = context.getDirectory().toPath();
    if (Files.exists(repoDir)) {
      try (OutputStream os = request.getArchive().openStream()) {
        addPathToTar(repoDir, os).run();
      }
    } else {
      throw new ExportFailedException(
        ContextEntry.ContextBuilder.noContext(),
        "Could not export repository. Repository directory does not exist."
      );
    }
    return new BundleResponse(0);
  }

  @Override
  public String getFileExtension() {
    return TAR_ARCHIVE;
  }

  public interface Factory {
    HgBundleCommand create(HgCommandContext context);
  }

}

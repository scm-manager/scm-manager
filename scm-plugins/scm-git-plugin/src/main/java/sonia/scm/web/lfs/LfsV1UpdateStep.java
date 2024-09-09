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

package sonia.scm.web.lfs;

import jakarta.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.update.BlobDirectoryAccess;
import sonia.scm.version.Version;

import java.nio.file.Path;

@Extension
public class LfsV1UpdateStep implements UpdateStep {

  private final BlobDirectoryAccess blobDirectoryAccess;

  @Inject
  public LfsV1UpdateStep(BlobDirectoryAccess blobDirectoryAccess) {
    this.blobDirectoryAccess = blobDirectoryAccess;
  }

  @Override
  public void doUpdate() throws Exception {
    blobDirectoryAccess.forBlobDirectories(
      f -> {
        Path v1Directory = f.getFileName();
        String v1DirectoryName = v1Directory.toString();
        if (v1DirectoryName.endsWith("-git-lfs")) {
          blobDirectoryAccess.moveToRepositoryBlobStore(f, v1DirectoryName, v1DirectoryName.substring(0, v1DirectoryName.length() - "-git-lfs".length()));
        }
      }
    );
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.git.lfs";
  }
}

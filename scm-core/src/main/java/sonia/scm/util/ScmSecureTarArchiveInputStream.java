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

package sonia.scm.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class wraps the TarAchiveInputStream of apache commons compress.
 * The purpose is to add security relevant logic for handling tar archive entries.
 * Such as disallowing tar archive entries, that contain a path traversal string in their name.
 * This allows other components that consume tar archives to focus on their business logic
 * without having to worry about security relevant aspects such as path traversal.
 *
 * @see org.apache.commons.compress.archivers.tar.TarArchiveInputStream
 */
class ScmSecureTarArchiveInputStream extends TarArchiveInputStream {

  public ScmSecureTarArchiveInputStream(InputStream inputStream) {
    super(inputStream);
  }

  /**
   * Before returning the result of {@link TarArchiveInputStream#getNextEntry()},
   * this method checks the entry for security relevant issues.
   * Such as the tar archive entry name, containing a path traversal string.
   *
   * @return TarArchiveEntry parsed by the TarArchiveInputStream
   * @throws IOException If the next entry could not be read
   * @see TarArchiveInputStream#getNextEntry()
   */
  @Override
  public TarArchiveEntry getNextEntry() throws IOException {
    TarArchiveEntry entry = super.getNextEntry();

    if (entry != null && !ValidationUtil.isPathValid(entry.getName())) {
      throw new InsecureTarArchiveEntryException(
        String.format("Entry name contains illegal path traversal '%s'", entry.getName())
      );
    }

    return entry;
  }
}

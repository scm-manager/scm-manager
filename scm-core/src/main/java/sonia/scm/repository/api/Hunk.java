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

package sonia.scm.repository.api;

public interface Hunk extends Iterable<DiffLine> {

  default String getRawHeader() {
    return String.format("@@ -%s +%s @@", getLineMarker(getOldStart(), getOldLineCount()), getLineMarker(getNewStart(), getNewLineCount()));
  }

  default String getLineMarker(int start, int lineCount) {
    if (lineCount == 1) {
      return Integer.toString(start);
    } else {
      return String.format("%s,%s", start, lineCount);
    }
  }

  int getOldStart();

  int getOldLineCount();

  int getNewStart();

  int getNewLineCount();
}

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

import sonia.scm.repository.api.DiffLine;
import sonia.scm.repository.api.Hunk;

import java.util.Iterator;
import java.util.List;

public class GitHunk implements Hunk {

  private final FileRange oldFileRange;
  private final FileRange newFileRange;
  private List<DiffLine> lines;

  public GitHunk(FileRange oldFileRange, FileRange newFileRange) {
    this.oldFileRange = oldFileRange;
    this.newFileRange = newFileRange;
  }

  @Override
  public int getOldStart() {
    return oldFileRange.getStart();
  }

  @Override
  public int getOldLineCount() {
    return oldFileRange.getLineCount();
  }

  @Override
  public int getNewStart() {
    return newFileRange.getStart();
  }

  @Override
  public int getNewLineCount() {
    return newFileRange.getLineCount();
  }

  @Override
  public Iterator<DiffLine> iterator() {
    return lines.iterator();
  }

  void setLines(List<DiffLine> lines) {
    this.lines = lines;
  }
}

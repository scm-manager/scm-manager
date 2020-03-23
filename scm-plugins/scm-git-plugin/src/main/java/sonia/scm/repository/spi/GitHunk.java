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

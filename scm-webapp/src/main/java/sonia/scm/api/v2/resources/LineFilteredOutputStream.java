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

package sonia.scm.api.v2.resources;

import java.io.IOException;
import java.io.OutputStream;

class LineFilteredOutputStream extends OutputStream {
  private final OutputStream target;
  private final int start;
  private final Integer end;

  private Character lastLineBreakCharacter;
  private int currentLine = 0;

  LineFilteredOutputStream(OutputStream target, Integer start, Integer end) {
    this.target = target;
    this.start = start == null ? 0 : start;
    this.end = end == null ? Integer.MAX_VALUE : end;
  }

  @Override
  public void write(int b) throws IOException {
    switch (b) {
      case '\n':
      case '\r':
        if (lastLineBreakCharacter == null) {
          keepLineBreakInMind((char) b);
        } else if (lastLineBreakCharacter == b) {
          if (currentLine > start && currentLine <= end) {
            target.write('\n');
          }
          ++currentLine;
        } else {
          if (currentLine > start && currentLine <= end) {
            target.write('\n');
          }
          lastLineBreakCharacter = null;
        }
        break;
      default:
        if (lastLineBreakCharacter != null && currentLine > start && currentLine <= end) {
          target.write('\n');
        }
        lastLineBreakCharacter = null;
        if (currentLine >= start && currentLine < end) {
          target.write(b);
        }
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (currentLine > end) {
      return;
    }
    super.write(b, off, len);
  }

  public void keepLineBreakInMind(char b) {
    lastLineBreakCharacter = b;
    ++currentLine;
  }

  @Override
  public void close() throws IOException {
    if (lastLineBreakCharacter != null && currentLine >= start && currentLine < end) {
      target.write('\n');
    }
    target.close();
  }
}

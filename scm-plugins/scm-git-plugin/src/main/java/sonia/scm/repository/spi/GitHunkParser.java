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

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Scanner;

import static java.util.OptionalInt.of;

final class GitHunkParser {
  private static final int HEADER_PREFIX_LENGTH = "@@ -".length();
  private static final int HEADER_SUFFIX_LENGTH = " @@".length();

  private GitHunk currentGitHunk = null;
  private List<DiffLine> collectedLines = null;
  private int oldLineCounter = 0;
  private int newLineCounter = 0;

  GitHunkParser() {
  }

  public List<Hunk> parse(String content) {
    List<Hunk> hunks = new ArrayList<>();

    try (Scanner scanner = new Scanner(content).useDelimiter("\n")) {
      while (scanner.hasNext()) {
        String line = scanner.next();
        if (line.startsWith("@@")) {
          parseHeader(hunks, line);
        } else if (currentGitHunk != null) {
          parseDiffLine(line);
        }
      }
    }
    if (currentGitHunk != null) {
      currentGitHunk.setLines(collectedLines);
    }

    return hunks;
  }

  private void parseHeader(List<Hunk> hunks, String line) {
    if (currentGitHunk != null) {
      currentGitHunk.setLines(collectedLines);
    }
    String hunkHeader = line.substring(HEADER_PREFIX_LENGTH, line.length() - HEADER_SUFFIX_LENGTH);
    String[] split = hunkHeader.split("\\s");

    FileRange oldFileRange = createFileRange(split[0]);
    // TODO merge contains two two block which starts with "-" e.g. -1,3 -2,4 +3,6
    // check if it is relevant for our use case
    FileRange newFileRange = createFileRange(split[1]);

    currentGitHunk = new GitHunk(oldFileRange, newFileRange);
    hunks.add(currentGitHunk);

    collectedLines = new ArrayList<>();
    oldLineCounter = currentGitHunk.getOldStart();
    newLineCounter = currentGitHunk.getNewStart();
  }

  private void parseDiffLine(String line) {
    String content = line.substring(1);
    switch (line.charAt(0)) {
      case ' ':
        collectedLines.add(new UnchangedGitDiffLine(newLineCounter, oldLineCounter, content));
        ++newLineCounter;
        ++oldLineCounter;
        break;
      case '+':
        collectedLines.add(new AddedGitDiffLine(newLineCounter, content));
        ++newLineCounter;
        break;
      case '-':
        collectedLines.add(new RemovedGitDiffLine(oldLineCounter, content));
        ++oldLineCounter;
        break;
      default:
        if (!line.equals("\\ No newline at end of file")) {
          throw new IllegalStateException("cannot handle diff line: " + line);
        }
    }
  }

  private static class AddedGitDiffLine implements DiffLine {
    private final int newLineNumber;
    private final String content;

    private AddedGitDiffLine(int newLineNumber, String content) {
      this.newLineNumber = newLineNumber;
      this.content = content;
    }

    @Override
    public OptionalInt getOldLineNumber() {
      return OptionalInt.empty();
    }

    @Override
    public OptionalInt getNewLineNumber() {
      return of(newLineNumber);
    }

    @Override
    public String getContent() {
      return content;
    }
  }

  private static class RemovedGitDiffLine implements DiffLine {
    private final int oldLineNumber;
    private final String content;

    private RemovedGitDiffLine(int oldLineNumber, String content) {
      this.oldLineNumber = oldLineNumber;
      this.content = content;
    }

    @Override
    public OptionalInt getOldLineNumber() {
      return of(oldLineNumber);
    }

    @Override
    public OptionalInt getNewLineNumber() {
      return OptionalInt.empty();
    }

    @Override
    public String getContent() {
      return content;
    }
  }

  private static class UnchangedGitDiffLine implements DiffLine {
    private final int newLineNumber;
    private final int oldLineNumber;
    private final String content;

    private UnchangedGitDiffLine(int newLineNumber, int oldLineNumber, String content) {
      this.newLineNumber = newLineNumber;
      this.oldLineNumber = oldLineNumber;
      this.content = content;
    }

    @Override
    public OptionalInt getOldLineNumber() {
      return of(oldLineNumber);
    }

    @Override
    public OptionalInt getNewLineNumber() {
      return of(newLineNumber);
    }

    @Override
    public String getContent() {
      return content;
    }
  }

  private static FileRange createFileRange(String fileRangeString) {
    int start;
    int lineCount = 1;
    int commaIndex = fileRangeString.indexOf(',');
    if (commaIndex > 0) {
      start = Integer.parseInt(fileRangeString.substring(0, commaIndex));
      lineCount = Integer.parseInt(fileRangeString.substring(commaIndex + 1));
    } else {
      start = Integer.parseInt(fileRangeString);
    }

    return new FileRange(start, lineCount);
  }
}

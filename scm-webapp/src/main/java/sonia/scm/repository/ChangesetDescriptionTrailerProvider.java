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

package sonia.scm.repository;

import com.google.common.collect.ImmutableSet;
import sonia.scm.plugin.Extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
public class ChangesetDescriptionTrailerProvider implements ChangesetPreProcessorFactory {

  private static final Collection<String> SUPPORTED_TRAILER_TYPES = ImmutableSet.of("Co-authored-by", "Reviewed-by", "Signed-off-by", "Committed-by");
  private static final Pattern TRAILER_PATTERN = Pattern.compile("^([\\w-]*):\\W*(.*)\\W+<(.*)>\\W*$");

  @Override
  public ChangesetPreProcessor createPreProcessor(Repository repository) {
    return new TrailerChangesetPreProcessor();
  }

  private static class TrailerChangesetPreProcessor implements ChangesetPreProcessor {

    private final StringBuilder newDescription = new StringBuilder();

    private Changeset changeset;

    boolean foundEmptyLine;

    @Override
    public void process(Changeset changeset) {
      this.changeset = changeset;
      try (Scanner scanner = new Scanner(changeset.getDescription())) {
        while (scanner.hasNextLine()) {
          handleLine(scanner, scanner.nextLine());
        }
      }
      changeset.setDescription(newDescription.toString());
    }

    public void handleLine(Scanner scanner, String line) {
      if (line.trim().isEmpty()) {
        handleEmptyLine(scanner, line);
        return;
      }

      if (foundEmptyLine && checkForTrailer(line)) {
        return;
      }
      appendLine(scanner, line);
    }

    public boolean checkForTrailer(String line) {
      Matcher matcher = TRAILER_PATTERN.matcher(line);
      if (matcher.matches()) {
        String type = matcher.group(1);
        String name = matcher.group(2);
        String mail = matcher.group(3);
        if (SUPPORTED_TRAILER_TYPES.contains(type)) {
          createTrailer(type, name, mail);
          return true;
        }
      }
      return false;
    }

    public void handleEmptyLine(Scanner scanner, String line) {
      foundEmptyLine = true;
      appendLine(scanner, line);
    }

    public void appendLine(Scanner scanner, String line) {
      newDescription.append(line);
      if (scanner.hasNextLine()) {
        newDescription.append('\n');
      }
    }

    private void createTrailer(String type, String name, String mail) {
        changeset.addTrailer(new Trailer(type, new Person(name, mail)));
    }
  }
}

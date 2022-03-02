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

import sonia.scm.plugin.Extension;

import java.util.Optional;
import java.util.Scanner;

@Extension
public class ChangesetDescriptionContributorProvider implements ChangesetPreProcessorFactory {

  @Override
  public ChangesetPreProcessor createPreProcessor(Repository repository) {
    return new ContributorChangesetPreProcessor();
  }

  private static class ContributorChangesetPreProcessor implements ChangesetPreProcessor {
    @Override
    public void process(Changeset changeset) {
      new Worker(changeset).process();
    }
  }

  private static class Worker {
    private final StringBuilder newDescription = new StringBuilder();

    private final Changeset changeset;

    boolean foundEmptyLine;

    private Worker(Changeset changeset) {
      this.changeset = changeset;
    }
    private void process() {
      try (Scanner scanner = new Scanner(changeset.getDescription())) {
        while (scanner.hasNextLine()) {
          handleLine(scanner, scanner.nextLine());
        }
      }
      changeset.setDescription(newDescription.toString());
    }

    private void handleLine(Scanner scanner, String line) {
      if (line.trim().isEmpty()) {
        handleEmptyLine(scanner, line);
        return;
      }

      if (foundEmptyLine && checkForContributor(line)) {
        return;
      }
      appendLine(scanner, line);
    }

    private boolean checkForContributor(String line) {
      Optional<Contributor> contributor = Contributor.fromCommitLine(line);
      if (contributor.isPresent()) {
        changeset.addContributor(contributor.get());
        return true;
      } else{
        return false;
      }
    }

    private void handleEmptyLine(Scanner scanner, String line) {
      foundEmptyLine = true;
      appendLine(scanner, line);
    }

    private void appendLine(Scanner scanner, String line) {
      newDescription.append(line);
      if (scanner.hasNextLine()) {
        newDescription.append('\n');
      }
    }
  }
}

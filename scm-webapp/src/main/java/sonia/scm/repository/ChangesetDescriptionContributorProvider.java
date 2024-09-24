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
      Optional<Contributor> optionalContributor = Contributor.fromCommitLine(line);
      if (optionalContributor.isPresent()) {
        Contributor contributor = optionalContributor.get();
        if (acceptContributor(contributor)) {
          changeset.addContributor(contributor);
        }
        return true;
      }
      return false;
    }

    private boolean acceptContributor(Contributor contributor) {
      if (isCoAuthorEqualToAuthor(contributor)) {
        return false;
      }

      return
        changeset.getContributors().stream()
        .noneMatch(c ->
          c.getType().equals(contributor.getType())
            && c.getPerson().getMail().equals(contributor.getPerson().getMail()
          )
        );
    }

    private boolean isCoAuthorEqualToAuthor(Contributor contributor) {
      return contributor.getType().equals(Contributor.CO_AUTHORED_BY)
        && contributor.getPerson().getMail().equals(changeset.getAuthor().getMail());
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

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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.util.QuotedString;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;


public class GitDiffCommand extends AbstractGitCommand implements DiffCommand {

  @Inject
  GitDiffCommand(@Assisted GitContext context) {
    super(context);
  }

  @Override
  public DiffCommandBuilder.OutputStreamConsumer getDiffResult(DiffCommandRequest request) throws IOException {
    @SuppressWarnings("squid:S2095") // repository will be closed with the RepositoryService
    org.eclipse.jgit.lib.Repository repository = open();

    Differ.Diff diff = Differ.diff(repository, request);

    return output -> {
      try (DiffFormatter formatter = new DiffFormatter(new DequoteOutputStream(output))) {
        formatter.setRepository(repository);
        if (request.getIgnoreWhitespaceLevel() == IgnoreWhitespaceLevel.ALL) {
          formatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        }

        for (DiffEntry e : diff.getEntries()) {
          if (idOrPathChanged(e)) {
            formatter.format(e);
          }
        }

        formatter.flush();
      }
    };
  }

  private boolean idOrPathChanged(DiffEntry e) {
    return !e.getOldId().equals(e.getNewId()) || !e.getNewPath().equals(e.getOldPath());
  }

  static class DequoteOutputStream extends OutputStream {

    private static final String[] DEQUOTE_STARTS = {
      "--- ",
      "+++ ",
      "diff --git "
    };

    private final OutputStream target;

    private boolean afterNL = true;
    private boolean writeToBuffer = false;
    private int numberOfPotentialBeginning = -1;
    private int potentialBeginningCharCount = 0;
    private boolean inPotentialQuotedLine = false;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    DequoteOutputStream(OutputStream target) {
      this.target = new BufferedOutputStream(target);
    }

    @Override
    public void write(int i) throws IOException {
      if (i == (int) '\n') {
        handleNewLine(i);
        return;
      }

      if (afterNL) {
        afterNL = false;
        if (foundPotentialBeginning(i)) {
          return;
        }
        numberOfPotentialBeginning = -1;
        inPotentialQuotedLine = false;
      }

      if (inPotentialQuotedLine && i == '"') {
        handleQuote();
        return;
      }

      if (numberOfPotentialBeginning > -1 && checkForFurtherBeginning(i)) {
        return;
      }

      if (writeToBuffer) {
        buffer.write(i);
      } else {
        target.write(i);
      }
    }

    private boolean checkForFurtherBeginning(int i) throws IOException {
      if (i == DEQUOTE_STARTS[numberOfPotentialBeginning].charAt(potentialBeginningCharCount)) {
        if (potentialBeginningCharCount + 1 < DEQUOTE_STARTS[numberOfPotentialBeginning].length()) {
          ++potentialBeginningCharCount;
        } else {
          inPotentialQuotedLine = true;
        }
        target.write(i);
        return true;
      } else {
        numberOfPotentialBeginning = -1;
      }
      return false;
    }

    private boolean foundPotentialBeginning(int i) throws IOException {
      for (int n = 0; n < DEQUOTE_STARTS.length; ++n) {
        if (i == DEQUOTE_STARTS[n].charAt(0)) {
          numberOfPotentialBeginning = n;
          potentialBeginningCharCount = 1;
          target.write(i);
          return true;
        }
      }
      return false;
    }

    private void handleQuote() throws IOException {
      if (writeToBuffer) {
        buffer.write('"');
        dequoteBuffer();
      } else {
        writeToBuffer = true;
        buffer.reset();
        buffer.write('"');
      }
    }

    private void handleNewLine(int i) throws IOException {
      afterNL = true;
      if (writeToBuffer) {
        dequoteBuffer();
      }
      target.write(i);
    }

    private void dequoteBuffer() throws IOException {
      byte[] bytes = buffer.toByteArray();
      String dequote = QuotedString.GIT_PATH.dequote(bytes, 0, bytes.length);
      target.write(dequote.getBytes(UTF_8));
      writeToBuffer = false;
    }

    @Override
    public void flush() throws IOException {
      target.flush();
    }
  }

  public interface Factory {
    DiffCommand create(GitContext context);
  }

}

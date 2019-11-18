/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.repository.spi;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.util.QuotedString;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffCommandBuilder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitDiffCommand extends AbstractGitCommand implements DiffCommand {

  GitDiffCommand(GitContext context, Repository repository) {
    super(context, repository);
  }

  @Override
  public DiffCommandBuilder.OutputStreamConsumer getDiffResult(DiffCommandRequest request) throws IOException {
    @SuppressWarnings("squid:S2095") // repository will be closed with the RepositoryService
    org.eclipse.jgit.lib.Repository repository = open();

    Differ.Diff diff = Differ.diff(repository, request);

    return output -> {
      try (DiffFormatter formatter = new DiffFormatter(new DequoteOutputStream(output))) {
        formatter.setRepository(repository);

        for (DiffEntry e : diff.getEntries()) {
          if (!e.getOldId().equals(e.getNewId())) {
            formatter.format(e);
          }
        }

        formatter.flush();
      }
    };
  }

  static class DequoteOutputStream extends OutputStream {

    private final OutputStream target;

    private boolean afterNL = false;
    private boolean writeToBuffer = false;
    private int minusCount = 0;
    private int plusCount = 0;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    DequoteOutputStream(OutputStream target) {
      this.target = new BufferedOutputStream(target);
    }

    @Override
    public void write(int i) throws IOException {
      if (i == (int) '+' && afterNL) {
        plusCount = 1;
        afterNL = false;
        target.write(i);
      } else if (i == (int) '+' && plusCount > 0) {
        ++plusCount;
        afterNL = false;
        target.write(i);
      } else if (i == (int) '-' && afterNL) {
        minusCount = 1;
        afterNL = false;
        target.write(i);
      } else if (i == (int) '-' && minusCount > 0) {
        ++minusCount;
        afterNL = false;
        target.write(i);
      } else if (i == (int) ' ' && plusCount == 3) {
        writeToBuffer = true;
        buffer.reset();
        afterNL = false;
        plusCount = 0;
        target.write(i);
      } else if (i == (int) ' ' && minusCount == 3) {
        minusCount = 0;
        afterNL = false;
        writeToBuffer = true;
        buffer.reset();
        target.write(i);
      } else if (i == (int) '\n') {
        afterNL = true;
        if (writeToBuffer) {
          byte[] bytes = buffer.toByteArray();
          String dequote = QuotedString.GIT_PATH.dequote(bytes, 0, bytes.length);
          target.write(dequote.getBytes(UTF_8));
          writeToBuffer  = false;
        }
        target.write(i);
      } else if (writeToBuffer) {
        buffer.write(i);
        afterNL = false;
      } else {
        target.write(i);
        afterNL = false;
        minusCount = 0;
        plusCount = 0;
      }
    }

    @Override
    public void flush() throws IOException {
      target.flush();
    }
  }
}

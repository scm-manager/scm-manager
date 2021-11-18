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

import org.javahg.Repository;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.spi.javahg.HgDiffInternalCommand;
import sonia.scm.web.HgUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgDiffCommand extends AbstractCommand implements DiffCommand {

  HgDiffCommand(HgCommandContext context) {
    super(context);
  }

  @Override
  public DiffCommandBuilder.OutputStreamConsumer getDiffResult(DiffCommandRequest request) {
    return output -> {
      Repository hgRepo = open();
      try {
        diff(hgRepo, request, output);
      } finally {
        getContext().close();
      }
    };
  }

  @SuppressWarnings("UnstableApiUsage")
  private void diff(Repository hgRepo, DiffCommandRequest request, OutputStream output) throws IOException {
    HgDiffInternalCommand cmd = createDiffCommand(hgRepo, request);
    try (InputStream inputStream = streamDiff(hgRepo, cmd, request.getPath())) {
      ByteStreams.copy(inputStream, output);
    }
  }

  @Nonnull
  private HgDiffInternalCommand createDiffCommand(Repository hgRepo, DiffCommandRequest request) {
    HgDiffInternalCommand cmd = HgDiffInternalCommand.on(hgRepo);
    DiffFormat format = request.getFormat();
    if (format == DiffFormat.GIT) {
      cmd.git();
    }
    cmd.change(HgUtil.getRevision(request.getRevision()));
    return cmd;
  }

  private InputStream streamDiff(Repository hgRepo, HgDiffInternalCommand cmd, String path) {
    if (!Strings.isNullOrEmpty(path)) {
      return cmd.stream(hgRepo.file(path));
    } else {
      return cmd.stream();
    }
  }
}

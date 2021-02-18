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

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import sonia.scm.ContextEntry;
import sonia.scm.repository.api.BundleResponse;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.util.Archives;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static sonia.scm.util.Archives.addPathToTar;

public class GitBundleCommand extends AbstractGitCommand implements BundleCommand {

  private static final String TAR_ARCHIVE = "tar";

  public GitBundleCommand(GitContext context) {
    super(context);
  }

  @Override
  public BundleResponse bundle(BundleCommandRequest request) throws IOException {
    Path repoDir = context.getDirectory().toPath();
    if (Files.exists(repoDir)) {
      try (OutputStream os = request.getArchive().openStream();
           TarArchiveOutputStream taos = Archives.writeTarStream(os)) {

        addPathToTar(repoDir, taos).withFilter(this::shouldIncludeFile).run();
        taos.finish();
      }
    } else {
      throw new ExportFailedException(
        ContextEntry.ContextBuilder.noContext(),
        "Could not export repository. Repository directory does not exist."
      );
    }
    return new BundleResponse(0);
  }

  @Override
  public String getFileExtension() {
    return TAR_ARCHIVE;
  }

  private boolean shouldIncludeFile(Path filePath) {
    return !filePath.getFileName().toString().equals("config");
  }
}

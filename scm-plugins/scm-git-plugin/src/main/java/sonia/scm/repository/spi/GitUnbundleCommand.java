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

import com.google.common.io.ByteSource;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.api.UnbundleResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.google.common.base.Preconditions.checkNotNull;

public class GitUnbundleCommand extends AbstractGitCommand implements UnbundleCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitUnbundleCommand.class);

  GitUnbundleCommand(GitContext context) {
    super(context);
  }

  @Override
  public UnbundleResponse unbundle(UnbundleCommandRequest request) throws IOException {
    ByteSource archive = checkNotNull(request.getArchive(),"archive is required");
    Path repositoryDir = context.getDirectory().toPath();
    LOG.debug("archive repository {} to {}", repositoryDir, archive);

    if (!Files.exists(repositoryDir)) {
      Files.createDirectories(repositoryDir);
    }

    unbundleRepositoryFromRequest(request, repositoryDir);
    return new UnbundleResponse(0);
  }

  private void unbundleRepositoryFromRequest(UnbundleCommandRequest request, Path repositoryDir) throws IOException {
    try (TarArchiveInputStream tais = new TarArchiveInputStream(request.getArchive().openBufferedStream())) {
      TarArchiveEntry entry = tais.getNextTarEntry();
      while (entry != null) {
        createDirectoriesIfNestedFile(repositoryDir, entry);
        Path filePath = repositoryDir.resolve(entry.getName());
        if (!Files.exists(filePath)) {
          Files.createFile(filePath);
        }
        Files.copy(tais, filePath, StandardCopyOption.REPLACE_EXISTING);
        entry = tais.getNextTarEntry();
      }
    }
  }

  private void createDirectoriesIfNestedFile(Path repositoryDir, TarArchiveEntry entry) throws IOException {
    if (entry.getName().contains("/")) {
      Path filePath = Paths.get(entry.getName());
      if (!Files.exists(filePath)) {
          Files.createDirectories(repositoryDir.resolve(filePath));
        }
    }
  }
}

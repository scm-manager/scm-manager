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

import com.google.inject.assistedinject.Assisted;
import sonia.scm.ContextEntry;
import sonia.scm.repository.api.BundleResponse;
import sonia.scm.repository.api.ExportFailedException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static sonia.scm.util.Archives.addPathToTar;

public class HgBundleCommand implements BundleCommand {

  private static final String TAR_ARCHIVE = "tar";
  private final HgCommandContext context;

  @Inject
  public HgBundleCommand(@Assisted HgCommandContext context) {
    this.context = context;
  }

  @Override
  public BundleResponse bundle(BundleCommandRequest request) throws IOException {
    Path repoDir = context.getDirectory().toPath();
    if (Files.exists(repoDir)) {
      try (OutputStream os = request.getArchive().openStream()) {
        addPathToTar(repoDir, os).run();
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

  public interface Factory {
    HgBundleCommand create(HgCommandContext context);
  }

}

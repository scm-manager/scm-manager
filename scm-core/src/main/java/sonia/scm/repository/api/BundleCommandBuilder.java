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

package sonia.scm.repository.api;


import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryExportingCheck;
import sonia.scm.repository.spi.BundleCommand;
import sonia.scm.repository.spi.BundleCommandRequest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The bundle command dumps a repository to a byte source such as a file. The
 * created bundle can be restored to an empty repository with the
 * {@link UnbundleCommandBuilder}.
 *
 * @since 1.43
 */
public final class BundleCommandBuilder {

  private static final Logger logger =
    LoggerFactory.getLogger(BundleCommandBuilder.class);

  private final BundleCommand bundleCommand;

  private final Repository repository;

  private final RepositoryExportingCheck repositoryExportingCheck;

  BundleCommandBuilder(BundleCommand bundleCommand, RepositoryExportingCheck repositoryExportingCheck, Repository repository) {
    this.bundleCommand = bundleCommand;
    this.repositoryExportingCheck = repositoryExportingCheck;
    this.repository = repository;
  }


  /**
   * Dumps the repository to the given {@link File}.
   *
   * @param outputFile output file
   * @return bundle response
   */
  public BundleResponse bundle(File outputFile) {
    checkArgument((outputFile != null) && !outputFile.exists(),
      "file is null or exists already");

    BundleCommandRequest request =
      new BundleCommandRequest(Files.asByteSink(outputFile));

    logger.info("create bundle at {} for repository {}", outputFile,
      repository.getId());

    return bundleWithExportingLock(request);
  }

  /**
   * Dumps the repository to the given {@link OutputStream}.
   *
   * @param outputStream output stream
   * @return bundle response
   */
  public BundleResponse bundle(OutputStream outputStream) {
    checkNotNull(outputStream, "output stream is required");

    logger.info("bundle {} to output stream", repository);

    BundleCommandRequest request = new BundleCommandRequest(asByteSink(outputStream));
    return bundleWithExportingLock(request);
  }

  /**
   * Dumps the repository to the given {@link ByteSink}.
   *
   * @param sink byte sink
   * @return bundle response
   */
  public BundleResponse bundle(ByteSink sink) {
    checkNotNull(sink, "byte sink is required");
    logger.info("bundle {} to byte sink", sink);

    BundleCommandRequest request = new BundleCommandRequest(sink);
    return bundleWithExportingLock(request);
  }

  private BundleResponse bundleWithExportingLock(BundleCommandRequest request) {
    return repositoryExportingCheck.withExportingLock(repository, () -> {
      try {
        return bundleCommand.bundle(request);
      } catch (IOException e) {
        throw new InternalRepositoryException(repository, "Exception during bundle; does not necessarily indicate a problem with the repository", e);
      }
    });
  }

  /**
   * Checks for the file extension of the bundled repository
   */
  public String getFileExtension() {
    return bundleCommand.getFileExtension();
  }

  /**
   * Converts an {@link OutputStream} into a {@link ByteSink}.
   *
   * @param outputStream ouput stream to convert
   * @return converted byte sink
   */
  private ByteSink asByteSink(final OutputStream outputStream) {
    return new ByteSink() {

      @Override
      public OutputStream openStream() throws IOException {
        return outputStream;
      }
    };
  }

}

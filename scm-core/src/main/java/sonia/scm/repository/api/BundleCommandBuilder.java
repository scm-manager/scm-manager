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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.BundleCommand;
import sonia.scm.repository.spi.BundleCommandRequest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

//~--- JDK imports ------------------------------------------------------------

/**
 * The bundle command dumps a repository to a byte source such as a file. The
 * created bundle can be restored to an empty repository with the
 * {@link UnbundleCommandBuilder}.
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 * @since 1.43
 */
public final class BundleCommandBuilder
{

  /** logger for BundleCommandBuilder */
  private static final Logger logger =
    LoggerFactory.getLogger(BundleCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link BundleCommandBuilder}.
   *
   *
   * @param bundleCommand bundle command implementation
   * @param repository repository
   */
  BundleCommandBuilder(BundleCommand bundleCommand, Repository repository)
  {
    this.bundleCommand = bundleCommand;
    this.repository = repository;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Dumps the repository to the given {@link File}.
   *
   * @param outputFile output file
   *
   * @return bundle response
   *
   * @throws IOException
   */
  public BundleResponse bundle(File outputFile) throws IOException {
    checkArgument((outputFile != null) &&!outputFile.exists(),
      "file is null or exists already");

    BundleCommandRequest request =
      new BundleCommandRequest(Files.asByteSink(outputFile));

    logger.info("create bundle at {} for repository {}", outputFile,
      repository.getId());

    return bundleCommand.bundle(request);
  }

  /**
   * Dumps the repository to the given {@link OutputStream}.
   *
   *
   * @param outputStream output stream
   *
   * @return bundle response
   *
   * @throws IOException
   */
  public BundleResponse bundle(OutputStream outputStream)
    throws IOException
  {
    checkNotNull(outputStream, "output stream is required");

    logger.info("bundle {} to output stream", repository);

    return bundleCommand.bundle(
      new BundleCommandRequest(asByteSink(outputStream)));
  }

  /**
   * Dumps the repository to the given {@link ByteSink}.
   *
   * @param sink byte sink
   *
   * @return bundle response
   *
   * @throws IOException
   */
  public BundleResponse bundle(ByteSink sink)
    throws IOException
  {
    checkNotNull(sink, "byte sink is required");
    logger.info("bundle {} to byte sink", sink);

    return bundleCommand.bundle(new BundleCommandRequest(sink));
  }

  /**
   * Converts an {@link OutputStream} into a {@link ByteSink}.
   *
   *
   * @param outputStream ouput stream to convert
   *
   * @return converted byte sink
   */
  private ByteSink asByteSink(final OutputStream outputStream)
  {
    return new ByteSink()
    {

      @Override
      public OutputStream openStream() throws IOException
      {
        return outputStream;
      }
    };
  }

  //~--- fields ---------------------------------------------------------------

  /** bundle command implementation */
  private final BundleCommand bundleCommand;

  /** repository */
  private final Repository repository;
}

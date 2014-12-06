/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.ByteSink;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.spi.BundleCommand;
import sonia.scm.repository.spi.BundleCommandRequest;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

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
   * @throws RepositoryException
   */
  public BundleResponse bundle(File outputFile)
    throws IOException, RepositoryException
  {
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
   * @throws RepositoryException
   */
  public BundleResponse bundle(OutputStream outputStream)
    throws IOException, RepositoryException
  {
    checkNotNull(outputStream, "output stream is required");

    logger.info("bundle {} to output stream", repository.getId());

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
   * @throws RepositoryException
   */
  public BundleResponse bundle(ByteSink sink)
    throws IOException, RepositoryException
  {
    checkNotNull(sink, "byte sink is required");
    logger.info("bundle {} to byte sink");

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

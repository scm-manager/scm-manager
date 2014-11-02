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

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.spi.UnbundleCommand;
import sonia.scm.repository.spi.UnbundleCommandRequest;

import static com.google.common.base.Preconditions.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 * @since 1.43
 */
public final class UnbundleCommandBuilder
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(UnbundleCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param unbundleCommand
   * @param repository
   */
  public UnbundleCommandBuilder(UnbundleCommand unbundleCommand,
    Repository repository)
  {
    this.unbundleCommand = unbundleCommand;
    this.repository = repository;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param inputFile
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public UnbundleResponse unbundle(File inputFile)
    throws IOException, RepositoryException
  {
    checkArgument((inputFile != null) && inputFile.exists(),
      "existing file is required");

    UnbundleCommandRequest request =
      new UnbundleCommandRequest(Files.asByteSource(inputFile));

    logger.info("unbundle archive {} at {}", inputFile, repository.getId());

    return unbundleCommand.unbundle(request);
  }

  /**
   * Method description
   *
   *
   * @param inputStream
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public UnbundleResponse unbundle(InputStream inputStream)
    throws IOException, RepositoryException
  {
    checkNotNull(inputStream, "input stream is required");
    logger.info("unbundle archive from stream");

    return unbundleCommand.unbundle(
      new UnbundleCommandRequest(asByteSource(inputStream)));
  }

  /**
   * Method description
   *
   *
   * @param byteSource
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public UnbundleResponse unbundle(ByteSource byteSource)
    throws IOException, RepositoryException
  {
    checkNotNull(byteSource, "byte source is required");
    logger.info("unbundle from byte source");

    return unbundleCommand.unbundle(new UnbundleCommandRequest(byteSource));
  }

  /**
   * Method description
   *
   *
   * @param inputStream
   *
   * @return
   */
  private ByteSource asByteSource(final InputStream inputStream)
  {
    return ByteStreams.asByteSource(new InputSupplier<InputStream>()
    {

      @Override
      public InputStream getInput() throws IOException
      {
        return inputStream;
      }
    });
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Repository repository;

  /** Field description */
  private final UnbundleCommand unbundleCommand;
}

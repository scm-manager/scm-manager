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

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.UnbundleCommand;
import sonia.scm.repository.spi.UnbundleCommandRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

//~--- JDK imports ------------------------------------------------------------

/**
 * The unbundle command can restore an empty repository from a bundle. The
 * bundle can be created with the {@link BundleCommandBuilder}.
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 * @since 1.43
 */
public final class UnbundleCommandBuilder
{

  /** logger for UnbundleCommandBuilder */
  private static final Logger logger =
    LoggerFactory.getLogger(UnbundleCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new UnbundleCommandBuilder.
   *
   *
   * @param unbundleCommand unbundle command implementation
   * @param repository repository
   */
  public UnbundleCommandBuilder(UnbundleCommand unbundleCommand,
    Repository repository)
  {
    this.unbundleCommand = unbundleCommand;
    this.repository = repository;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Restores the repository from the given bundle.
   *
   *
   * @param inputFile input file
   *
   * @return unbundle response
   *
   * @throws IOException
   */
  public UnbundleResponse unbundle(File inputFile)
    throws IOException
  {
    checkArgument((inputFile != null) && inputFile.exists(),
      "existing file is required");

    UnbundleCommandRequest request =
      createRequest(Files.asByteSource(inputFile));

    logger.info("unbundle archive {} at {}", inputFile, repository);

    return unbundleCommand.unbundle(request);
  }

  /**
   * Restores the repository from the given bundle.
   *
   *
   * @param inputStream input stream
   *
   * @return unbundle response
   *
   * @throws IOException
   */
  public UnbundleResponse unbundle(InputStream inputStream)
    throws IOException
  {
    checkNotNull(inputStream, "input stream is required");
    logger.info("unbundle archive from stream");

    return unbundleCommand.unbundle(createRequest(asByteSource(inputStream)));
  }

  /**
   * Restores the repository from the given bundle.
   *
   *
   * @param byteSource byte source
   *
   * @return unbundle response
   *
   * @throws IOException
   */
  public UnbundleResponse unbundle(ByteSource byteSource)
    throws IOException
  {
    checkNotNull(byteSource, "byte source is required");
    logger.info("unbundle from byte source");

    return unbundleCommand.unbundle(createRequest(byteSource));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Set to {@code true} if bundle is gzip compressed. Default is {@code false}.
   *
   *
   * @param compressed {@code true} if bundle is gzip compressed
   *
   * @return {@code this}
   */
  public UnbundleCommandBuilder setCompressed(boolean compressed)
  {
    this.compressed = compressed;

    return this;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Converts an {@link InputStream} into a {@link ByteSource}.
   *
   *
   * @param inputStream input stream
   *
   * @return byte source
   */
  private ByteSource asByteSource(final InputStream inputStream)
  {
    return new ByteSource()
    {

      @Override
      public InputStream openStream()
      {
        return inputStream;
      }
    };
  }

  /**
   * Creates the {@link UnbundleCommandRequest}.
   *
   *
   * @param source byte source
   *
   * @return the create request
   */
  private UnbundleCommandRequest createRequest(ByteSource source)
  {
    ByteSource bs;

    if (compressed)
    {
      logger.debug("decode gzip stream for unbundle command");
      bs = new CompressedByteSource(source);
    }
    else
    {
      bs = source;
    }

    return new UnbundleCommandRequest(bs);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * ByteSource which is able to handle gzip compressed resources.
   */
  private static class CompressedByteSource extends ByteSource
  {

    /**
     * Constructs ...
     *
     *
     * @param wrapped
     */
    public CompressedByteSource(ByteSource wrapped)
    {
      this.wrapped = wrapped;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Opens the stream for reading the compressed source.
     *
     *
     * @return input stream
     *
     * @throws IOException
     */
    @Override
    public InputStream openStream() throws IOException
    {
      return new GZIPInputStream(wrapped.openStream());
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final ByteSource wrapped;
  }


  //~--- fields ---------------------------------------------------------------

  /** repository */
  private final Repository repository;

  /** unbundle command implementation */
  private final UnbundleCommand unbundleCommand;

  /** Field description */
  private boolean compressed = false;
}

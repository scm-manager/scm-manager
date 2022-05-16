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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Feature;
import sonia.scm.repository.spi.DiffCommand;
import sonia.scm.repository.spi.DiffCommandRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * Shows differences between revisions for a specified file or
 * the entire revision.<br />
 * <b>Note:</b> One of the parameter path or revision have to be set.<br />
 * <br />
 * <b>Sample:</b>
 * <br />
 * <br />
 * Print the differences from revision 33b93c443867:<br />
 * <pre><code>
 * DiffCommandBuilder diff = repositoryService.getDiffCommand();
 * String content = diff.setRevision("33b93c443867").getContent();
 * System.out.println(content);
 * </code></pre>
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class DiffCommandBuilder extends AbstractDiffCommandBuilder<DiffCommandBuilder, DiffCommandRequest>
{

  /**
   * the logger for DiffCommandBuilder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DiffCommandBuilder.class);

  /** implementation of the diff command */
  private final DiffCommand diffCommand;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link DiffCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param diffCommand implementation of {@link DiffCommand}
   * @param supportedFeatures The supported features of the provider
   */
  DiffCommandBuilder(DiffCommand diffCommand, Set<Feature> supportedFeatures)
  {
    super(supportedFeatures);
    this.diffCommand = diffCommand;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Passes the difference of the given parameter to the outputstream.
   *
   *
   * @return A consumer that expects the output stream for the difference
   *
   * @throws IOException
   */
  public OutputStreamConsumer retrieveContent() throws IOException {
    return getDiffResult();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the content of the difference as string.
   *
   * @return content of the difference
   *
   * @throws IOException
   */
  public String getContent() throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      getDiffResult().accept(baos);
      return baos.toString();
    }
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the diff format which should be used for the output.
   * <strong>Note: </strong> If the repository provider does not support the
   * diff format, it will fallback to its default format.
   *
   *
   * @param format format of the diff output
   *
   * @return {@code this}
   *
   * @since 1.34
   */
  public DiffCommandBuilder setFormat(DiffFormat format)
  {
    Preconditions.checkNotNull(format, "format could not be null");
    request.setFormat(format);

    return this;
  }
  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   * @return
   */
  private OutputStreamConsumer getDiffResult() throws IOException {
    Preconditions.checkArgument(request.isValid(),
      "path and/or revision is required");

    logger.debug("create diff for {}", request);

    return diffCommand.getDiffResult(request);
  }

  @Override
  DiffCommandBuilder self() {
    return this;
  }

  @Override
  DiffCommandRequest createRequest() {
    return new DiffCommandRequest();
  }

  @FunctionalInterface
  public interface OutputStreamConsumer {
    void accept(OutputStream outputStream) throws IOException;
  }
}

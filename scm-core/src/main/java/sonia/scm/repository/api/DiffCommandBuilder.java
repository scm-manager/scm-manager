/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.api;


import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Feature;
import sonia.scm.repository.spi.DiffCommand;
import sonia.scm.repository.spi.DiffCommandRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

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
 * @since 1.17
 */
public final class DiffCommandBuilder extends AbstractDiffCommandBuilder<DiffCommandBuilder, DiffCommandRequest>
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(DiffCommandBuilder.class);

  private final DiffCommand diffCommand;


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


  /**
   * Returns the content of the difference as string.
   *
   * @return content of the difference
   *
   * @throws IOException
   */
  public String getContent() throws IOException {
    checkArguments();

    logger.debug("create diff content for {}", request);

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      diffCommand.getDiffResultInternal(request).accept(baos);
      return baos.toString(StandardCharsets.UTF_8);
    }
  }


  /**
   * Sets the diff format which should be used for the output.
   * <strong>Note: </strong> If the repository provider does not support the
   * diff format, it will fall back to its default format.
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

  private OutputStreamConsumer getDiffResult() throws IOException {
    checkArguments();

    logger.debug("create diff for {}", request);

    return diffCommand.getDiffResult(request);
  }

  private void checkArguments() {
    Preconditions.checkArgument(request.isValid(),
      "path and/or revision is required");
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

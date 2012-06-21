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

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.spi.DiffCommand;
import sonia.scm.repository.spi.DiffCommandRequest;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
 * 
 * TODO check current behavior.
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class DiffCommandBuilder
{

  /**
   * the logger for DiffCommandBuilder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DiffCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link DiffCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param implementation of {@link DiffCommand}
   *
   * @param diffCommand
   */
  DiffCommandBuilder(DiffCommand diffCommand)
  {
    this.diffCommand = diffCommand;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Passes the difference of the given parameter to the outputstream.
   *
   *
   * @param outputStream outputstream for the difference
   *
   * @return {@code this}
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public DiffCommandBuilder retriveContent(OutputStream outputStream)
          throws IOException, RepositoryException
  {
    getDiffResult(outputStream);

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the content of the difference as string.
   *
   * @return content of the difference
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public String getContent() throws IOException, RepositoryException
  {
    String content = null;
    ByteArrayOutputStream baos = null;

    try
    {
      baos = new ByteArrayOutputStream();
      getDiffResult(baos);
      content = baos.toString();
    }
    finally
    {
      Closeables.closeQuietly(baos);
    }

    return content;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Show the difference only for the given path.
   *
   *
   * @param path path for difference
   *
   * @return {@code this}
   */
  public DiffCommandBuilder setPath(String path)
  {
    request.setPath(path);

    return this;
  }

  /**
   * Show the difference only for the given revision.
   *
   *
   * @param revision revision for difference
   *
   * @return {@code this}
   */
  public DiffCommandBuilder setRevision(String revision)
  {
    request.setRevision(revision);

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param outputStream
   * @param path
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private void getDiffResult(OutputStream outputStream)
          throws IOException, RepositoryException
  {
    Preconditions.checkNotNull(outputStream, "OutputStream is required");
    Preconditions.checkArgument(request.isValid(),
                                "path and/or revision is required");

    if (logger.isDebugEnabled())
    {
      logger.debug("create diff for {}", request);
    }

    diffCommand.getDiffResult(request, outputStream);
  }

  //~--- fields ---------------------------------------------------------------

  /** implementation of the diff command */
  private DiffCommand diffCommand;

  /** request for the diff command implementation */
  private DiffCommandRequest request = new DiffCommandRequest();
}

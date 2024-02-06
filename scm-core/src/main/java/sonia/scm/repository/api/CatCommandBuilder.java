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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.CatCommand;
import sonia.scm.repository.spi.CatCommandRequest;
import sonia.scm.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Shows the content of a file in the {@link Repository}.<br />
 * <br />
 * <b>Sample:</b>
 * <br />
 * <br />
 * Print the content of the file core/pom.xml from revision 46a23689ac91:<br />
 * <pre><code>
 * CatCommandBuilder cat = repositoryService.getCatCommand();
 * String content = cat.setRevision("46a23689ac91").getContent("core/pom.xml");
 * System.out.println(content);
 * </code></pre>
 *
 * @since 1.17
 */
public final class CatCommandBuilder
{
  private static final Logger logger =
    LoggerFactory.getLogger(CatCommandBuilder.class);

  private final CatCommand catCommand;

  private final CatCommandRequest request = new CatCommandRequest();

  /**
   * Constructs a new {@link CatCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param catCommand implementation of the {@link CatCommand}
   */
  CatCommandBuilder(CatCommand catCommand)
  {
    this.catCommand = catCommand;
  }


  /**
   * Reset each parameter to its default value.
   *
   * @return {@code this}
   */
  public CatCommandBuilder reset()
  {
    request.reset();

    return this;
  }

  /**
   * Passes the content of the given file to the output stream.
   *
   * @param outputStream output stream for the content
   * @param path file path
   */
  public void retriveContent(OutputStream outputStream, String path) throws IOException {
    getCatResult(outputStream, path);
  }

  /**
   * Returns an output stream with the file content.
   *
   * @param path file path
   */
  public InputStream getStream(String path) throws IOException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(path),
      "path is required");

    CatCommandRequest requestClone = request.clone();

    requestClone.setPath(path);

    logger.debug("create cat stream for {}", requestClone);

    return catCommand.getCatResultStream(requestClone);
  }

  /**
   * Returns the content of the given file.
   */
  public String getContent(String path) throws IOException {
    String content = null;
    ByteArrayOutputStream baos = null;

    try
    {
      baos = new ByteArrayOutputStream();
      getCatResult(baos, path);
      content = baos.toString();
    }
    finally
    {
      IOUtil.close(baos);
    }

    return content;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Sets the revision of the file.
   *
   * @return {@code this}
   */
  public CatCommandBuilder setRevision(String revision)
  {
    request.setRevision(revision);

    return this;
  }


  /**
   * Executes the cat command.
   *
   *
   * @param outputStream the outputstream for the content
   * @param path path of the file
   *
   * @throws IOException
   */
  private void getCatResult(OutputStream outputStream, String path)
    throws IOException {
    Preconditions.checkNotNull(outputStream, "OutputStream is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(path),
      "path is required");

    CatCommandRequest requestClone = request.clone();

    requestClone.setPath(path);

    if (logger.isDebugEnabled())
    {
      logger.debug("create cat for {}", requestClone);
    }

    catCommand.getCatResult(requestClone, outputStream);
  }

}

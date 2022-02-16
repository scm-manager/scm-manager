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

package sonia.scm.repository.spi.javahg;

//~--- non-JDK imports --------------------------------------------------------

import org.javahg.Repository;
import org.javahg.internals.AbstractCommand;
import org.javahg.internals.HgInputStream;
import sonia.scm.repository.FileObject;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.util.Optional;

/**
 * Mercurial command to list files of a repository.
 *
 * @author Sebastian Sdorra
 */
public class HgFileviewCommand extends AbstractCommand
{

  private boolean disableLastCommit = false;

  private HgFileviewCommand(Repository repository)
  {
    super(repository);
  }

  /**
   * Create command for the given repository.
   *
   * @param repository repository
   *
   * @return fileview command
   */
  public static HgFileviewCommand on(Repository repository)
  {
    return new HgFileviewCommand(repository);
  }

  /**
   * Disable last commit fetching for file objects.
   *
   * @return {@code this}
   */
  public HgFileviewCommand disableLastCommit() {
    disableLastCommit = true;
    cmdAppend("-d");

    return this;
  }

  /**
   * Disables sub repository detection
   *
   * @return {@code this}
   */
  public HgFileviewCommand disableSubRepositoryDetection() {
    cmdAppend("-s");

    return this;
  }

  /**
   * Start file object fetching at the given path.
   *
   *
   * @param path path to start fetching
   *
   * @return {@code this}
   */
  public HgFileviewCommand path(String path) {
    cmdAppend("-p", path);

    return this;
  }

  /**
   * Fetch file objects recursive.
   *
   *
   * @return {@code this}
   */
  public HgFileviewCommand recursive() {
    cmdAppend("-c");

    return this;
  }

  /**
   * Use given revision for file view.
   *
   * @param revision revision id, hash, tag or branch
   *
   * @return {@code this}
   */
  public HgFileviewCommand rev(String revision) {
    cmdAppend("-r", revision);

    return this;
  }

  /**
   * Limit the number of result files to <code>limit</code> entries.
   *
   * @param limit The maximal number of files this request shall return.
   *
   * @return {@code this}
   * @since 2.0.0
   */
  public HgFileviewCommand setLimit(int limit) {
    cmdAppend("-l", limit);

    return this;
  }

  /**
   * Proceed the list from the given number on (zero based).
   *
   * @param offset The number of the entry, the result should start with (zero based).
   *               All preceding entries will be omitted.
   *
   * @return {@code this}
   * @since 2.0.0
   */
  public HgFileviewCommand setOffset(int offset) {
    cmdAppend("-o", offset);

    return this;
  }

  /**
   * Executes the mercurial command and parses the output.
   *
   * @return file object
   *
   * @throws IOException
   */
  public Optional<FileObject> execute() throws IOException
  {
    cmdAppend("-t");

    HgInputStream stream = launchStream();

    try {
      return new HgFileviewCommandResultReader(stream, disableLastCommit).parseResult();
    } finally {
      stream.close();
    }
  }

  /**
   * Returns the name of the mercurial command.
   *
   * @return command name
   */
  @Override
  public String getCommandName()
  {
    return HgFileviewExtension.NAME;
  }
}

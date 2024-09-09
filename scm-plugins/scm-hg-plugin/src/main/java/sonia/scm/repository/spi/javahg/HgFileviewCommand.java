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

package sonia.scm.repository.spi.javahg;


import org.javahg.Repository;
import org.javahg.internals.AbstractCommand;
import org.javahg.internals.HgInputStream;
import sonia.scm.repository.FileObject;

import java.io.IOException;
import java.util.Optional;

/**
 * Mercurial command to list files of a repository.
 *
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
   */
  public HgFileviewCommand disableLastCommit() {
    disableLastCommit = true;
    cmdAppend("-d");

    return this;
  }

  /**
   * Disables sub repository detection
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
   */
  @Override
  public String getCommandName()
  {
    return HgFileviewExtension.NAME;
  }
}

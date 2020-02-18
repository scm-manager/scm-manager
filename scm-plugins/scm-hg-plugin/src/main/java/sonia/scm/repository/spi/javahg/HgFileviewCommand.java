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



package sonia.scm.repository.spi.javahg;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.DateTime;
import com.aragost.javahg.Repository;
import com.aragost.javahg.internals.AbstractCommand;
import com.aragost.javahg.internals.HgInputStream;

import com.google.common.base.Strings;

import sonia.scm.repository.FileObject;
import sonia.scm.repository.SubRepository;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Mercurial command to list files of a repository.
 *
 * @author Sebastian Sdorra
 */
public class HgFileviewCommand extends AbstractCommand
{

  public static final char TRUNCATED_MARK = 't';
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
   * @param proceedFrom The number of the entry, the result should start with (zero based).
   *                    All preceding entries will be omitted.
   *
   * @return {@code this}
   * @since 2.0.0
   */
  public HgFileviewCommand setProceedFrom(int proceedFrom) {
    cmdAppend("-f", proceedFrom);

    return this;
  }

  /**
   * Executes the mercurial command and parses the output.
   *
   * @return file object
   *
   * @throws IOException
   */
  public FileObject execute() throws IOException
  {
    cmdAppend("-t");

    Deque<FileObject> stack = new LinkedList<>();

    HgInputStream stream = launchStream();

    FileObject last = null;
    while (stream.peek() != -1 && stream.peek() != TRUNCATED_MARK) {
      FileObject file = read(stream);

      while (!stack.isEmpty()) {
        FileObject current = stack.peek();
        if (isParent(current, file)) {
          current.addChild(file);
          break;
        } else {
          stack.pop();
        }
      }

      if (file.isDirectory()) {
        stack.push(file);
      }
      last = file;
    }

    if (stack.isEmpty()) {
      // if the stack is empty, the requested path is probably a file
      return last;
    } else {
      // if the stack is not empty, the requested path is a directory
      if (stream.read() == TRUNCATED_MARK) {
        stack.getLast().setTruncated(true);
      }
      return stack.getLast();
    }
  }

  private FileObject read(HgInputStream stream) throws IOException {
    char type = (char) stream.read();

    FileObject file;
    switch (type) {
      case 'd':
        file = readDirectory(stream);
        break;
      case 'f':
        file = readFile(stream);
        break;
      case 's':
        file = readSubRepository(stream);
        break;
      default:
        throw new IOException("unknown file object type: " + type);
    }
    return file;
  }

  private boolean isParent(FileObject parent, FileObject child) {
    String parentPath = parent.getPath();
    if (parentPath.equals("")) {
      return true;
    }
    return child.getParentPath().equals(parentPath);
  }

  private FileObject readDirectory(HgInputStream stream) throws IOException {
    FileObject directory = new FileObject();
    String path = removeTrailingSlash(stream.textUpTo('\0'));

    directory.setName(getNameFromPath(path));
    directory.setDirectory(true);
    directory.setPath(path);

    return directory;
  }

  private FileObject readFile(HgInputStream stream) throws IOException {
    FileObject file = new FileObject();
    String path = removeTrailingSlash(stream.textUpTo('\n'));

    file.setName(getNameFromPath(path));
    file.setPath(path);
    file.setDirectory(false);
    file.setLength((long) stream.decimalIntUpTo(' '));

    DateTime timestamp = stream.dateTimeUpTo(' ');
    String description = stream.textUpTo('\0');

    if (!disableLastCommit) {
      file.setCommitDate(timestamp.getDate().getTime());
      file.setDescription(description);
    }

    return file;
  }

  private FileObject readSubRepository(HgInputStream stream) throws IOException {
    FileObject directory = new FileObject();
    String path = removeTrailingSlash(stream.textUpTo('\n'));

    directory.setName(getNameFromPath(path));
    directory.setDirectory(true);
    directory.setPath(path);

    String revision = stream.textUpTo(' ');
    String url = stream.textUpTo('\0');

    SubRepository subRepository = new SubRepository(url);

    if (!Strings.isNullOrEmpty(revision)) {
      subRepository.setRevision(revision);
    }

    directory.setSubRepository(subRepository);

    return directory;
  }

  private String removeTrailingSlash(String path) {
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    return path;
  }

  private String getNameFromPath(String path) {
    int index = path.lastIndexOf('/');

    if (index > 0) {
      path = path.substring(index + 1);
    }

    return path;
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

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

import com.aragost.javahg.DateTime;
import com.aragost.javahg.internals.HgInputStream;
import com.google.common.base.Strings;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.SubRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

class HgFileviewCommandResultReader {

  private static final char TRUNCATED_MARK = 't';

  private final HgInputStream stream;
  private final boolean disableLastCommit;

  HgFileviewCommandResultReader(HgInputStream stream, boolean disableLastCommit) {
    this.stream = stream;
    this.disableLastCommit = disableLastCommit;
  }

  Optional<FileObject> parseResult() throws IOException {
    Deque<FileObject> stack = new LinkedList<>();

    FileObject last = null;
    while (stream.peek() != -1 && stream.peek() != TRUNCATED_MARK) {
      FileObject file = read(stream);

      while (!stack.isEmpty()) {
        FileObject current = stack.peek();
        if (isParent(current, file)) {
          current.addChild(file);
          break;
        } else if (isAncestor(current, file)) {
          Collection<FileObject> missingParents = createMissingParents(current, file);
          for (FileObject subDir : missingParents) {
            current.addChild(subDir);
            stack.push(subDir);
            current = stack.peek();
          }
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
      return of(last);
    } else if (stack.size() == 1 && stack.getFirst().isDirectory() && stack.getFirst().getChildren().isEmpty()) {
      // There are no empty directories in hg. When we get this,
      // we just get the requested path as a directory, but it does not exist.
      return empty();
    } else {
      // if the stack is not empty, the requested path is a directory
      if (stream.read() == TRUNCATED_MARK) {
        stack.getLast().setTruncated(true);
      }
      return of(stack.getLast());
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
    return child.getParentPath().equals(parentPath);
  }

  private boolean isAncestor(FileObject ancestor, FileObject child) {
    String ancestorPath = ancestor.getPath();
    return ancestorPath.equals("") || child.getParentPath().startsWith(ancestorPath + '/');
  }

  private Collection<FileObject> createMissingParents(FileObject current, FileObject file) {
    String missingPath = file.getPath().substring(current.getPath().length(), file.getPath().lastIndexOf('/'));

    FileObject directory = new FileObject();
    directory.setName(getNameFromPath(missingPath));
    directory.setDirectory(true);
    directory.setPath(missingPath);

    Collection<FileObject> parents = new ArrayList<>();

    if (!isParent(current, directory)) {
      parents.addAll(createMissingParents(current, directory));
    }

    parents.add(directory);

    return parents;
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
}

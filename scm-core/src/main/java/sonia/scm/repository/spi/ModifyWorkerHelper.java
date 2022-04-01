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

package sonia.scm.repository.spi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

/**
 * This "interface" is not really intended to be used as an interface but rather as
 * a base class to reduce code redundancy in Worker instances.
 */
public interface ModifyWorkerHelper extends ModifyCommand.Worker {

  Logger LOG = LoggerFactory.getLogger(ModifyWorkerHelper.class);

  @Override
  default void delete(String toBeDeleted, boolean recursive) throws IOException {
    Path fileToBeDeleted = getTargetFile(toBeDeleted);
    if (recursive) {
      IOUtil.delete(fileToBeDeleted.toFile());
    } else {
      try {
        Files.delete(fileToBeDeleted);
      } catch (NoSuchFileException e) {
        throw notFound(createFileContext(toBeDeleted));
      }
    }
    doScmDelete(toBeDeleted);
  }

  /**
   * @since 2.28.0
   */
  @Override
  default void move(String source, String target, boolean overwrite) throws IOException {
    Path sourceFile = getTargetFile(source);

    if (!Files.exists(sourceFile)) {
      throw notFound(entity("Path", source).in(getRepository()));
    }

    Path targetFile = getTargetFile(target);

    if (!overwrite && Files.exists(targetFile)) {
      throw alreadyExists(entity("Path", target).in(getRepository()));
    }

    doThrow()
      .violation("Cannot move if new path would be a child directory of path")
      .when(Files.isDirectory(sourceFile) && targetFile.startsWith(sourceFile));

    Files.createDirectories(targetFile.getParent());
    try {
      Path pathAfterMove = Files.move(sourceFile, targetFile, REPLACE_EXISTING);
      doScmMove(source, toScmPath(pathAfterMove));
    } catch (FileAlreadyExistsException e) {
      throw alreadyExists(entity("File", target).in(getRepository()));
    }
  }

  /**
   * @since 2.28.0
   */
  default String toScmPath(Path pathAfterMove) {
    String path = getWorkDir().toPath().relativize(pathAfterMove).normalize().toString();
    if (File.separator.equals("/")) {
      return path;
    }
    return path.replace(File.separator, "/");
  }

  /**
   * @since 2.28.0
   */
  default void doScmMove(String path, String newPath) {
    doScmDelete(path);
    addRecursive(newPath);
  }

  /**
   * @since 2.28.0
   */
  default void addRecursive(String path) {
    Path targetPath = getTargetFile(path);
    if (Files.isDirectory(targetPath)) {
      try (Stream<Path> list = Files.list(targetPath)) {
        list.forEach(subPath -> addRecursive(path + "/" + subPath.getFileName()));
      } catch (IOException e) {
        throw new InternalRepositoryException(getRepository(), "Could not add files to scm", e);
      }
    } else {
      addMovedFileToScm(path, targetPath);
    }
  }

  default void addMovedFileToScm(String path, Path targetPath) {
    addFileToScm(path, targetPath);
  }

  void doScmDelete(String toBeDeleted);

  @Override
  default void create(String toBeCreated, File file, boolean overwrite) throws IOException {
    Path targetFile = getTargetFile(toBeCreated);
    createDirectories(targetFile);
    if (overwrite) {
      Files.move(file.toPath(), targetFile, REPLACE_EXISTING);
    } else {
      try {
        Files.move(file.toPath(), targetFile);
      } catch (FileAlreadyExistsException e) {
        throw alreadyExists(createFileContext(toBeCreated));
      }
    }
    addFileToScm(toBeCreated, targetFile);
  }

  default void modify(String path, File file) throws IOException {
    Path targetFile = getTargetFile(path);
    createDirectories(targetFile);
    if (!targetFile.toFile().exists()) {
      throw notFound(createFileContext(path));
    }
    boolean executable = Files.isExecutable(targetFile);
    Files.move(file.toPath(), targetFile, REPLACE_EXISTING);
    if (!targetFile.toFile().setExecutable(executable)) {
      LOG.warn("could not set executable flag for file {}", targetFile);
    }
    addFileToScm(path, targetFile);
  }

  void addFileToScm(String name, Path file);

  default ContextEntry.ContextBuilder createFileContext(String path) {
    ContextEntry.ContextBuilder contextBuilder = entity("file", path);
    if (!StringUtils.isEmpty(getBranch())) {
      contextBuilder.in("branch", getBranch());
    }
    contextBuilder.in(getRepository());
    return contextBuilder;
  }

  default void createDirectories(Path targetFile) throws IOException {
    try {
      Files.createDirectories(targetFile.getParent());
    } catch (FileAlreadyExistsException e) {
      throw alreadyExists(createFileContext(targetFile.toString()));
    }
  }

  default Path getTargetFile(String path) {
    File workDir = getWorkDir();
    // WARNING: 'new File(workDir, path)' is not the same as
    // 'workDir.toPath().resolve(path)'! The first one does not
    // mind whether 'path' starts with a '/', the nio api does.
    // So using the file api the two paths '/file' and 'file'
    // lead to the same result, whereas the nio api would
    // lead to an absolute path starting at the system root in the
    // first example starting with a '/'.
    Path targetFile = new File(workDir, path).toPath().normalize();
    doThrow()
      .violation("illegal path traversal: " + path)
      .when(!targetFile.startsWith(workDir.toPath().normalize()));
    doThrow()
      .violation("protected path: " + path)
      .when(isProtectedPath(targetFile));
    return targetFile;
  }

  File getWorkDir();

  Repository getRepository();

  String getBranch();

  default boolean isProtectedPath(Path path) {
    return false;
  }
}

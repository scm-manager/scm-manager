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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.AlreadyExistsException;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModifyWorkerHelperTest {

  private static final Repository REPOSITORY = new Repository("42", "git", "hitchhiker", "hog");

  private boolean pathProtected = false;

  @Test
  void shouldKeepExecutableFlag(@TempDir Path temp) throws IOException {

    File target = createFile(temp, "executable.sh");
    File newFile = createFile(temp, "other");

    target.setExecutable(true);

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    helper.modify("executable.sh", newFile);

    assertThat(target.canExecute()).isTrue();
  }

  @Test
  void shouldNotWriteInProtectedPath(@TempDir Path temp) throws IOException {

    pathProtected = true;
    File target = createFile(temp, "some.txt");

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    assertThrows(
      ScmConstraintViolationException.class,
      () -> helper.create("some.txt", target, true)
    );
  }

  @Test
  void shouldNotWritePathWithPathTraversal(@TempDir Path temp) throws IOException {

    File target = createFile(temp, "some.txt");

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    assertThrows(
      ScmConstraintViolationException.class,
      () -> helper.create("../some.txt", target, true)
    );
  }

  @Test
  void shouldNotMoveNotExistingFile(@TempDir Path temp) throws IOException {
    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    assertThrows(
      NotFoundException.class,
      () -> helper.move("no-such-file", "irrelevant", false));
  }

  @Test
  void shouldNotOverwriteExistingFileInMoveWithoutOverwrite(@TempDir Path temp) throws IOException {
    createFile(temp, "existing-source.txt");
    createFile(temp, "existing-target.txt");

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    assertThrows(
      AlreadyExistsException.class,
      () -> helper.move("existing-source.txt", "existing-target.txt", false));
  }

  private File createFile(Path temp, String fileName) throws IOException {
    File file = new File(temp.toFile(), fileName);
    FileWriter source = new FileWriter(file);
    source.write("something");
    source.close();
    return file;
  }

  private class MinimalModifyWorkerHelper implements ModifyWorkerHelper {

    private final Path temp;

    public MinimalModifyWorkerHelper(Path temp) {
      this.temp = temp;
    }

    @Override
    public void doScmDelete(String toBeDeleted) {

    }

    @Override
    public void addFileToScm(String name, Path file) {

    }

    @Override
    public File getWorkDir() {
      return temp.toFile();
    }

    @Override
    public Repository getRepository() {
      return REPOSITORY;
    }

    @Override
    public String getBranch() {
      return null;
    }

    @Override
    public boolean isProtectedPath(Path path) {
      return pathProtected;
    }
  }
}

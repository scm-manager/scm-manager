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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TempDirectory.class)
class ModifyWorkerHelperTest {

  @Test
  void shouldKeepExecutableFlag(@TempDirectory.TempDir Path temp) throws IOException {

    File target = createFile(temp, "executable.sh");
    File newFile = createFile(temp, "other");

    target.setExecutable(true);

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    helper.modify("executable.sh", newFile);

    assertThat(target.canExecute()).isTrue();
  }

  private File createFile(Path temp, String fileName) throws IOException {
    File file = new File(temp.toFile(), fileName);
    FileWriter source = new FileWriter(file);
    source.write("something");
    source.close();
    return file;
  }

  private static class MinimalModifyWorkerHelper implements ModifyWorkerHelper {

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
      return null;
    }

    @Override
    public String getBranch() {
      return null;
    }
  }
}

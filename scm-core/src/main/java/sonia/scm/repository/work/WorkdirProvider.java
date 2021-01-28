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

package sonia.scm.repository.work;

import sonia.scm.repository.RepositoryLocationResolver;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorkdirProvider {

  private final File rootDirectory;
  private final RepositoryLocationResolver repositoryLocationResolver;

  @Inject
  public WorkdirProvider(RepositoryLocationResolver repositoryLocationResolver) {
    this(new File(System.getProperty("scm.workdir" , System.getProperty("java.io.tmpdir")), "scm-work"), repositoryLocationResolver);
  }

  public WorkdirProvider(File rootDirectory, RepositoryLocationResolver repositoryLocationResolver) {
    this.rootDirectory = rootDirectory;
    this.repositoryLocationResolver = repositoryLocationResolver;
    if (!rootDirectory.exists() && !rootDirectory.mkdirs()) {
      throw new IllegalStateException("could not create pool directory " + rootDirectory);
    }
  }

  public File createNewWorkdir() {
    return createWorkDir(this.rootDirectory);
  }

  public File createNewWorkdir(String repositoryId) {
    return createWorkDir(repositoryLocationResolver.forClass(Path.class).getLocation(repositoryId).toFile());
  }

  private File createWorkDir(File baseDirectory) {
    try {
      return Files.createTempDirectory(baseDirectory.toPath(),"workdir").toFile();
    } catch (IOException e) {
      throw new WorkdirCreationException(baseDirectory.toString(), e);
    }
  }
}

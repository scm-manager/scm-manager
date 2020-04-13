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

package sonia.scm.repository.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class WorkingCopy<R, W> implements AutoCloseable {

  private static final Logger LOG = LoggerFactory.getLogger(WorkingCopy.class);

  private final File directory;
  private final W workingRepository;
  private final R centralRepository;
  private final Consumer<W> cleanupWorkdir;
  private final Consumer<R> cleanupCentral;

  public WorkingCopy(W workingRepository, R centralRepository, Consumer<W> cleanupWorkdir, Consumer<R> cleanupCentral, File directory) {
    this.directory = directory;
    this.workingRepository = workingRepository;
    this.centralRepository = centralRepository;
    this.cleanupCentral = cleanupCentral;
    this.cleanupWorkdir = cleanupWorkdir;
  }

  public W getWorkingRepository() {
    return workingRepository;
  }

  public R getCentralRepository() {
    return centralRepository;
  }

  public File getDirectory() {
    return directory;
  }

  @Override
  public void close() {
    try {
      cleanupWorkdir.accept(workingRepository);
      cleanupCentral.accept(centralRepository);
      delete();
    } catch (IOException e) {
      LOG.warn("could not delete temporary workdir '{}'", directory, e);
    }
  }

  void delete() throws IOException {
    IOUtil.delete(directory);
  }
}

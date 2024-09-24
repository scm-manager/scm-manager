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

package sonia.scm.repository.work;

import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;

public final class WorkingCopy<R, W> implements AutoCloseable {

  private final File directory;
  private final W workingRepository;
  private final R centralRepository;
  private final Runnable close;

  public WorkingCopy(W workingRepository, R centralRepository, Runnable close, File directory) {
    this.directory = directory;
    this.workingRepository = workingRepository;
    this.centralRepository = centralRepository;
    this.close = close;
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
    close.run();
  }

  void delete() throws IOException {
    IOUtil.delete(directory);
  }
}

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

import com.google.inject.Inject;
import sonia.scm.util.IOUtil;

import java.io.File;

/**
 * This is the default implementation for the {@link WorkingCopyPool}. For each requested {@link WorkingCopy} with
 * {@link #getWorkingCopy(SimpleWorkingCopyFactory.WorkingCopyContext)}, a new directory is requested from the
 * {@link WorkdirProvider}. This directory is deleted immediately when the context is closed with
 * {@link #contextClosed(SimpleWorkingCopyFactory.WorkingCopyContext, File)}.
 */
public class NoneCachingWorkingCopyPool implements WorkingCopyPool {

  private final WorkdirProvider workdirProvider;

  @Inject
  public NoneCachingWorkingCopyPool(WorkdirProvider workdirProvider) {
    this.workdirProvider = workdirProvider;
  }

  @Override
  public <R, W> WorkingCopy<R, W> getWorkingCopy(SimpleWorkingCopyFactory<R, W, ?>.WorkingCopyContext context) {
    return context.initialize(workdirProvider.createNewWorkdir(context.getScmRepository().getId()));
  }

  @Override
  public void contextClosed(SimpleWorkingCopyFactory<?, ?, ?>.WorkingCopyContext workingCopyContext, File workdir) {
    IOUtil.deleteSilently(workdir);
  }

  @Override
  public void shutdown() {
    // no caches, nothing to clean up :-)
  }
}

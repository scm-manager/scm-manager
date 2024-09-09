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

package sonia.scm.repository.spi;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import sonia.scm.repository.SvnWorkingCopyFactory;
import sonia.scm.repository.work.SimpleWorkingCopyFactory;
import sonia.scm.repository.work.WorkingCopyPool;

import java.io.File;

public class SimpleSvnWorkingCopyFactory extends SimpleWorkingCopyFactory<File, File, SvnContext> implements SvnWorkingCopyFactory {

  @Inject
  public SimpleSvnWorkingCopyFactory(WorkingCopyPool workingCopyPool, MeterRegistry meterRegistry) {
    super(workingCopyPool, meterRegistry);
  }

  @Override
  protected ParentAndClone<File, File> initialize(SvnContext context, File workingCopy, String initialBranch) {
    return new SvnWorkingCopyInitializer(context).initialize(workingCopy);
  }

  @Override
  protected ParentAndClone<File, File> reclaim(SvnContext context, File target, String initialBranch) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    return new SvnWorkingCopyReclaimer(context).reclaim(target);
  }

  @Override
  protected void closeRepository(File workingCopy) {
    // No resources to be closed for svn
  }

  @Override
  protected void closeWorkingCopy(File workingCopy) {
    // No resources to be closed for svn
  }
}

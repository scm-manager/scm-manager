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

package sonia.scm.importexport;

import jakarta.inject.Inject;
import sonia.scm.Initable;
import sonia.scm.SCMContextProvider;
import sonia.scm.schedule.Scheduler;

public class ExportGarbageCollector implements Initable {

  private final ExportService exportService;
  private final Scheduler scheduler;

  @Inject
  public ExportGarbageCollector(ExportService exportService, Scheduler scheduler) {
    this.exportService = exportService;
    this.scheduler = scheduler;
  }

  @Override
  public void init(SCMContextProvider context) {
    scheduler.schedule("0 0 6 * * ?", exportService::cleanupOutdatedExports);
  }
}

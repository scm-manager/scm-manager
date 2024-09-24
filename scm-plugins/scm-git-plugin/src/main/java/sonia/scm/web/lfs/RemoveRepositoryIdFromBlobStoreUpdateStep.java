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

package sonia.scm.web.lfs;

import jakarta.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.StoreType;
import sonia.scm.update.RepositoryUpdateIterator;
import sonia.scm.update.StoreUpdateStepUtilFactory;
import sonia.scm.version.Version;

import static sonia.scm.version.Version.parse;

@Extension
public class RemoveRepositoryIdFromBlobStoreUpdateStep implements UpdateStep {

  private final RepositoryUpdateIterator repositoryUpdateIterator;
  private final StoreUpdateStepUtilFactory utilFactory;

  @Inject
  public RemoveRepositoryIdFromBlobStoreUpdateStep(RepositoryUpdateIterator repositoryUpdateIterator, StoreUpdateStepUtilFactory utilFactory) {
    this.repositoryUpdateIterator = repositoryUpdateIterator;
    this.utilFactory = utilFactory;
  }

  @Override
  public void doUpdate()  {
    repositoryUpdateIterator.forEachRepository(this::doUpdate);
  }

  private void doUpdate(String repositoryId) {
    utilFactory
      .forType(StoreType.BLOB)
      .forName(repositoryId + "-git-lfs")
      .forRepository(repositoryId)
      .build()
      .renameStore("git-lfs");
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.1");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.git.lfs";
  }
}

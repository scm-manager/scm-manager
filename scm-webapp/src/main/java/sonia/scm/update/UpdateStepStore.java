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

package sonia.scm.update;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.NamespaceUpdateStep;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.migration.UpdateStep;
import sonia.scm.migration.UpdateStepTarget;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

class UpdateStepStore {

  private static final String STORE_NAME = "executedUpdates";

  private static final Logger LOG = LoggerFactory.getLogger(UpdateStepStore.class);

  private final ConfigurationEntryStoreFactory storeFactory;

  @Inject
  UpdateStepStore(ConfigurationEntryStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  void storeExecutedUpdate(String repositoryId, RepositoryUpdateStep updateStep) {
    ConfigurationEntryStore<UpdateVersionInfo> store = createRepositoryStore(repositoryId);
    storeNewVersion(store, updateStep);
  }

  void storeExecutedUpdate(String namespace, NamespaceUpdateStep updateStep) {
    ConfigurationEntryStore<UpdateVersionInfo> store = createNamespaceStore(namespace);
    storeNewVersion(store, updateStep);
  }

  void storeExecutedUpdate(UpdateStep updateStep) {
    ConfigurationEntryStore<UpdateVersionInfo> store = createGlobalStore();
    storeNewVersion(store, updateStep);
  }

  boolean notRunYet(UpdateStep updateStep) {
    return notRunYet(createGlobalStore(), updateStep);
  }

  boolean notRunYet(String repositoryId, RepositoryUpdateStep updateStep) {
    return notRunYet(createRepositoryStore(repositoryId), updateStep);
  }

  boolean notRunYet(String namespace, NamespaceUpdateStep updateStep) {
    return notRunYet(createNamespaceStore(namespace), updateStep);
  }

  private void storeNewVersion(ConfigurationEntryStore<UpdateVersionInfo> store, UpdateStepTarget updateStep) {
    UpdateVersionInfo newVersionInfo = new UpdateVersionInfo(updateStep.getTargetVersion().getParsedVersion());
    store.put(updateStep.getAffectedDataType(), newVersionInfo);
  }

  private boolean notRunYet(ConfigurationEntryStore<UpdateVersionInfo> store, UpdateStepTarget updateStep) {
    UpdateVersionInfo updateVersionInfo = store.get(updateStep.getAffectedDataType());
    if (updateVersionInfo == null) {
      LOG.trace("no updates for type {} run yet; step will be executed", updateStep.getAffectedDataType());
      return true;
    }
    boolean result = updateStep.getTargetVersion().isNewer(updateVersionInfo.getLatestVersion());
    LOG.trace("latest version for type {}: {}; step will be executed: {}",
      updateStep.getAffectedDataType(),
      updateVersionInfo.getLatestVersion(),
      result
    );
    return result;
  }

  private ConfigurationEntryStore<UpdateVersionInfo> createGlobalStore() {
    return storeFactory
      .withType(UpdateVersionInfo.class)
      .withName(STORE_NAME)
      .build();
  }

  private ConfigurationEntryStore<UpdateVersionInfo> createRepositoryStore(String repositoryId) {
    return storeFactory
      .withType(UpdateVersionInfo.class)
      .withName(STORE_NAME)
      .forRepository(repositoryId)
      .build();
  }

  private ConfigurationEntryStore<UpdateVersionInfo> createNamespaceStore(String namespace) {
    return storeFactory
      .withType(UpdateVersionInfo.class)
      .withName(STORE_NAME)
      .forNamespace(namespace)
      .build();
  }
}

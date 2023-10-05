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

package sonia.scm.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.NamespaceUpdateStep;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.migration.UpdateStep;
import sonia.scm.migration.UpdateStepTarget;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import javax.inject.Inject;

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

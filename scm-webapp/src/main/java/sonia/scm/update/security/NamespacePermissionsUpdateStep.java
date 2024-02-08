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

package sonia.scm.update.security;

import jakarta.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.version.Version;

import java.util.HashSet;

@Extension
public class NamespacePermissionsUpdateStep implements UpdateStep {

  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;

  @Inject
  public NamespacePermissionsUpdateStep(ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
  }

  @Override
  public void doUpdate() throws Exception {
    ConfigurationEntryStore<AssignedPermission> securityStore = createSecurityStore();
    HashSet<String> toBeRemoved = new HashSet<>();
    HashSet<AssignedPermission> toBeAdded = new HashSet<>();
    securityStore.getAll().forEach((k, v) -> {
      if (v.getPermission().getValue().equals("namespace:permissionRead")) {
        toBeAdded.add(new AssignedPermission(v.getName(), v.isGroupPermission(), "namespace:permissionRead:*"));
        toBeRemoved.add(k);
      }
      if (v.getPermission().getValue().equals("namespace:permissionRead,permissionWrite")) {
        toBeAdded.add(new AssignedPermission(v.getName(), v.isGroupPermission(), "namespace:permissionRead,permissionWrite:*"));
        toBeRemoved.add(k);
      }
    });
    toBeAdded.forEach(securityStore::put);
    toBeRemoved.forEach(securityStore::remove);
  }

  private ConfigurationEntryStore<AssignedPermission> createSecurityStore() {
    return configurationEntryStoreFactory.withType(AssignedPermission.class).withName("security").build();
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("3.1.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.security.xml";
  }
}

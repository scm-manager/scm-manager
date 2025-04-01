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

import sonia.scm.store.QueryableMaintenanceStore;
import sonia.scm.store.StoreParameters;
import sonia.scm.store.StoreType;

public interface StoreUpdateStepUtilFactory {

  default UtilForTypeBuilder forType(StoreType type) {
    return new UtilForTypeBuilder(this, type);
  }

  <T> QueryableMaintenanceStore<T> forQueryableType(Class<T> clazz, String... parents);

  final class UtilForTypeBuilder {
    private final StoreUpdateStepUtilFactory factory;
    private final StoreType type;

    public UtilForTypeBuilder(StoreUpdateStepUtilFactory factory, StoreType type) {
      this.factory = factory;
      this.type = type;
    }

    public UtilForNameBuilder forName(String name) {
      return new UtilForNameBuilder(factory, type, name);
    }
  }

  final class UtilForNameBuilder {

    private final StoreUpdateStepUtilFactory factory;
    private final StoreType type;
    private final String name;
    private String repositoryId;
    private String namespace;

    public UtilForNameBuilder(StoreUpdateStepUtilFactory factory, StoreType type, String name) {
      this.factory = factory;
      this.type = type;
      this.name = name;
    }

    public UtilForNameBuilder forRepository(String repositoryId) {
      this.repositoryId = repositoryId;
      return this;
    }

    public UtilForNameBuilder forNamespace(String namespace) {
      this.namespace = namespace;
      return this;
    }

    public StoreUpdateStepUtil build() {
      return factory.build(
        type,
        new StoreParameters() {
          @Override
          public String getName() {
            return name;
          }

          @Override
          public String getRepositoryId() {
            return repositoryId;
          }

          @Override
          public String getNamespace() {
            return namespace;
          }
        }
      );
    }
  }

  StoreUpdateStepUtil build(StoreType type, StoreParameters parameters);

  interface StoreUpdateStepUtil {
    void renameStore(String newName);

    void deleteStore();
  }
}

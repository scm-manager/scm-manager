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

package sonia.scm.store;

/**
 * The fields of the {@link StoreParameters} are used from the {@link BlobStoreFactory} to create a store.
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public interface StoreParameters {

  String getName();
  String getRepositoryId();

  /**
   * Returns optional namespace to which the store is related.
   * @return namespace
   * @since 2.44.0
   */
  String getNamespace();

  /**
   * Whether the store that is supposed to be built, should ignore that the repository is read-only or not.
   * @return true if it should ignore the read-only property of a repository
   */
  default boolean isIgnoreReadOnly() {
    return false;
  }
}

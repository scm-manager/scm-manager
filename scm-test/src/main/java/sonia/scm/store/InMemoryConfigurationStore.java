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
 * In memory store implementation of {@link ConfigurationStore}.
 *
 *
 * @param <T> type of stored object
 *
 * @deprecated Replaced by the {@link InMemoryByteConfigurationStore}
 */
@Deprecated(since = "2.44.0")
public class InMemoryConfigurationStore<T> implements ConfigurationStore<T> {

  private T object;

  @Override
  public T get() {
    return object;
  }

  @Override
  public void set(T obejct) {
    this.object = obejct;
  }

  @Override
  public void delete() {
    object = null;
  }
}

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

import com.google.common.collect.Collections2;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * A ConfigurationEntryStore can be used to store multiple entries of structured
 * configuration data. <b>Note:</b> the default implementation use JAXB to
 * marshall the items.
 *
 *
 * @param <V> store value type
 * @since 1.31
 */
public interface ConfigurationEntryStore<V> extends DataStore<V> {

  /**
   * Return all values matching the given {@link Predicate}.
   *
   * Default implementation since 2.44.0
   *
   * @param predicate predicate to match values
   *
   * @return filtered collection of values
   */
  default Collection<V> getMatchingValues(Predicate<V> predicate) {
    return Collections2.filter(getAll().values(), predicate::test);
  }
}

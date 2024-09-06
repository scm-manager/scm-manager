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

import com.google.common.annotations.Beta;

import java.io.InputStream;

/**
 * The {@link StoreEntryImporter} is used to import a store entry from an {@link InputStream}.
 * <p><b>This interface is not yet finalized and might change in the upcoming versions.</b></p>
 *
 * @since 2.13.0
 */
@Beta
public interface StoreEntryImporter {

  /**
   * Will be called for each entry of the store.
   * @param name The name of the store entry.
   * @param stream An input stream with the raw data of the store entry.
   */
  void importEntry(String name, InputStream stream);
}

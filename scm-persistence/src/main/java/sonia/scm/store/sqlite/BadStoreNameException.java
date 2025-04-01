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

package sonia.scm.store.sqlite;

import sonia.scm.store.StoreException;

/**
 * This exception is thrown if a name for a store element doesn't meet the internal verification requirements.
 *
 * @since 3.7.0
 */
class BadStoreNameException extends StoreException {
  BadStoreNameException(String badName) {
    super("This name has been rejected: " + badName);
  }
}

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

import sonia.scm.store.QueryableStore;

import static sonia.scm.store.sqlite.SQLiteIdentifiers.computeColumnIdentifier;

final class SQLFieldHelper {

  private SQLFieldHelper() {
  }

  static String computeSQLField(QueryableStore.QueryField<?, ?> queryField) {
    if (queryField instanceof QueryableStore.CollectionSizeQueryField<?>) {
      return "json_array_length(payload, '$." + queryField.getName() + "')";
    } else if (queryField instanceof QueryableStore.MapSizeQueryField<?>) {
      return "(select count(*) from json_each(payload, '$." + queryField.getName() + "')) ";
    } else if (queryField.isIdField()) {
      return computeColumnIdentifier(queryField.getName());
    } else {
      return "json_extract(payload, '$." + queryField.getName() + "')";
    }
  }
}

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

import lombok.Getter;

/**
 * Representation of a value of a row within an {@link SQLTable}.
 *
 * @since 3.7.0
 */
@Getter
class SQLField implements SQLNode {

  static final SQLField PAYLOAD = new SQLField("json(payload)");

  private final String name;

  SQLField(String name) {
    this.name = name;
  }

  @Override
  public String toSQL() {
    return name;
  }
}

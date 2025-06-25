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

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Representation of a SQL COUNT field.
 *
 * @since 3.9.0
 */
@Getter
class SQLCountField implements SQLNode {

  private final Collection<SQLNode> fields;
  private final boolean distinct;

  public SQLCountField() {
    this(emptyList(), false);
  }

  SQLCountField(Collection<SQLNode> fields, boolean distinct) {
    this.fields = fields;
    this.distinct = distinct;
  }

  @Override
  public String toSQL() {
    StringBuilder sqlBuilder = new StringBuilder("COUNT(");
    if (distinct) {
      sqlBuilder.append("DISTINCT ");
    }
    if (fields.isEmpty()) {
      sqlBuilder.append("*");
    } else {
      sqlBuilder.append(fields.stream().map(SQLNode::toSQL).collect(Collectors.joining(", ")));
    }
    return sqlBuilder.append(")").toString();
  }
}

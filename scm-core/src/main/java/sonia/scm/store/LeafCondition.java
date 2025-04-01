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

import lombok.Getter;
import lombok.Value;

/**
 * A <b>LeafCondition</b> is a condition builder on a {@link QueryableStore.QueryField} as part of a store statement.
 *
 * @param <T> type of the object held by the {@link QueryableStore.QueryField}
 * @param <C> value type (only required for binary operators)
 */
@Value
@Getter
public class LeafCondition<T, C> implements Condition<T> {

  /**
   * Argument for the operator to check against.<br/>
   * Example: <em><strong>fruit</strong> EQ apple</em>
   */
  QueryableStore.QueryField<T, ?> field;

  /**
   * A binary (e.g. EQ, CONTAINS) or unary (e.g. NULL) operator. Binary operators require a non-null value field.<br/>
   * Example: <em>fruit <strong>EQ</strong> apple</em>, <em>fruit <em>NULL</em></em>
   */
  Operator operator;


  /**
   * Value for binary operators.<br/>
   * Example: <em>fruit EQ <strong>apple</strong></em>
   */
  C value;
}

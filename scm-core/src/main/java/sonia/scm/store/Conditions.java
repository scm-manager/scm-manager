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

public final class Conditions {
  private Conditions() {
  }

  @SafeVarargs
  public static <T> Condition<T> and(Condition<T>... conditions) {
    return new LogicalCondition<>(LogicalOperator.AND, conditions);
  }

  @SafeVarargs
  public static <T> Condition<T> or(Condition<T>... conditions) {
    return new LogicalCondition<>(LogicalOperator.OR, conditions);
  }

  @SafeVarargs
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> Condition<T> not(Condition<T>... conditions) {
    return new LogicalCondition<>(LogicalOperator.NOT, new Condition[]{and(conditions)});
  }
}

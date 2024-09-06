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

package sonia.scm.search;

import lombok.Value;

@Value
public class IndexParams implements IndexDetails {

  String index;
  LuceneSearchableType searchableType;

  @Override
  public Class<?> getType() {
    return searchableType.getType();
  }

  @Override
  public String getName() {
    return index;
  }

  @Override
  public String toString() {
    return searchableType.getName() + "/" + index;
  }

}

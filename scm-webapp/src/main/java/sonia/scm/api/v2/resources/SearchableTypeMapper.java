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

package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sonia.scm.search.SearchableField;
import sonia.scm.search.SearchableType;
import sonia.scm.search.TypeCheck;

@Mapper
public abstract class SearchableTypeMapper {

  @Mapping(target = "attributes", ignore = true)
  public abstract SearchableTypeDto map(SearchableType searchableType);

  public abstract SearchableFieldDto map(SearchableField searchableField);

  public String map(Class<?> type) {
    if (TypeCheck.isString(type)) {
      return "string";
    } else if (TypeCheck.isInstant(type) || TypeCheck.isNumber(type)) {
      return "number";
    } else if (TypeCheck.isBoolean(type)) {
      return "boolean";
    } else {
      return "unknown";
    }
  }

}

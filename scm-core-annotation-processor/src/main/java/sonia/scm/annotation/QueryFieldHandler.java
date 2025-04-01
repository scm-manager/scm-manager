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

package sonia.scm.annotation;

import com.squareup.javapoet.TypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
class QueryFieldHandler {
  private final String clazz;
  private final TypeName[] generics;
  private final FieldInitializer initializer;
  private final String suffix;

  public QueryFieldHandler(String clazz, TypeName[] generics, FieldInitializer initializer) {
    this(clazz, generics, initializer, null);
  }

  public Optional<String> getSuffix() {
    return Optional.ofNullable(suffix);
  }
}

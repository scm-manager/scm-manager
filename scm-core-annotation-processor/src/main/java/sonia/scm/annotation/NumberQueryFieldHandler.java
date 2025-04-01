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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

class NumberQueryFieldHandler extends QueryFieldHandler {
  public NumberQueryFieldHandler(String packageName, String className) {
    this(packageName, className, null);
  }

  public NumberQueryFieldHandler(String packageName, String className, String suffix) {
    super(
      "NumberQueryField",
      new TypeName[]{ClassName.get(packageName, className)},
      (fieldBuilder, element, fieldClass, fieldName) -> fieldBuilder
        .initializer(
          "new $T<>($S)",
          ClassName.get("sonia.scm.store", "QueryableStore").nestedClass(fieldClass),
          fieldName
        ),
      suffix
    );
  }
}

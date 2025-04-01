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

import com.google.common.base.Strings;
import sonia.scm.plugin.QueryableTypeDescriptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SQLiteIdentifiers {

  private static final Pattern PATTERN = Pattern.compile("^\\w+$");
  private static final String STORE_TABLE_SUFFIX = "_STORE";

  static String computeTableName(QueryableTypeDescriptor queryableTypeDescriptor) {
    if (Strings.isNullOrEmpty(queryableTypeDescriptor.getName())) {
      String className = queryableTypeDescriptor.getClazz();
      return sanitize(computeSqlIdentifier(removeClassSuffix(className)) + STORE_TABLE_SUFFIX);
    } else {
      return sanitize(queryableTypeDescriptor.getName() + STORE_TABLE_SUFFIX);
    }
  }

  static String computeColumnIdentifier(String className) {
    if (className == null) {
      return "ID";
    }
    String nameWithoutClassSuffix = removeClassSuffix(className);
    String classNameWithoutPackage = nameWithoutClassSuffix.substring(nameWithoutClassSuffix.lastIndexOf('.') + 1);
    return computeSqlIdentifier(classNameWithoutPackage) + "_ID";
  }

  private static String computeSqlIdentifier(String className) {
    return sanitize(className.replace("_", "__").replace('.', '_'));
  }

  private static String removeClassSuffix(String className) {
    if (className.endsWith(".class")) {
      return className.substring(0, className.length() - 6);
    }
    return className;
  }

  static String sanitize(String name) throws BadStoreNameException {
    Matcher matcher = PATTERN.matcher(name);
    if (!matcher.matches()) {
      throw new BadStoreNameException(name);
    } else {
      return name;
    }
  }
}

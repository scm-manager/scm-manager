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

package sonia.scm.repository;

import sonia.scm.plugin.Extension;
import sonia.scm.util.ValidationUtil;

import java.util.regex.Pattern;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Extension
public class CustomNamespaceStrategy implements NamespaceStrategy {

  private static final Pattern ONE_TO_THREE_DIGITS = Pattern.compile("[0-9]{1,3}");

  @Override
  public String createNamespace(Repository repository) {
    String namespace = repository.getNamespace();

    doThrow()
      .violation("invalid namespace", "namespace")
      .when(
        !ValidationUtil.isNameValid(namespace)
          || ONE_TO_THREE_DIGITS.matcher(namespace).matches()
          || namespace.equals("create")
          || namespace.equals("import")
          || namespace.equals("..")
      );

    return namespace;
  }

  @Override
  public boolean canBeChanged() {
    return true;
  }
}

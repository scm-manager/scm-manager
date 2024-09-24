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

package sonia.scm.cli;

import java.util.Arrays;
import java.util.Collections;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

class CombinedResourceBundle extends ListResourceBundle {

  private final Object[][] contents;

  @Override
  protected Object[][] getContents() {
    return contents;
  }

  public CombinedResourceBundle(ResourceBundle... bundles) {
    this.contents = Arrays
      .stream(bundles)
      .flatMap(resourceBundle ->
        Collections
          .list(resourceBundle.getKeys())
          .stream().map(key -> new Object[]{key, resourceBundle.getObject(key)}))
      .collect(Collectors.toList())
      .toArray(new Object[0][0]);
  }
}

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

package sonia.scm.repository.spi;


import sonia.scm.repository.BrowserResult;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 *
 * @since 1.17
 */
public interface BrowseCommand
{
  BrowserResult getBrowserResult(BrowseCommandRequest request) throws IOException;

  default <T> void sort(List<T> entries, Function<T, Boolean> isDirectory, Function<T, String> nameOf) {
    entries.sort((e1, e2) -> {
      if (isDirectory.apply(e1).equals(isDirectory.apply(e2))) {
        return nameOf.apply(e1).compareTo(nameOf.apply(e2));
      } else if (isDirectory.apply(e1)) {
        return -1;
      } else {
        return 1;
      }
    });
  }
}

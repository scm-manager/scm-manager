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

package sonia.scm.repository.spi.javahg;

import sonia.scm.repository.Added;
import sonia.scm.repository.Copied;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Modified;
import sonia.scm.repository.Removed;
import sonia.scm.repository.Renamed;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;

class HgModificationParser {
  private final Collection<Modification> modifications = new LinkedHashSet<>();

  void addLine(String line) {
    if (line.length() < 2) {
      return;
    }
    String linePrefix = line.substring(0, 2).toLowerCase(Locale.ROOT);
    switch (linePrefix) {
      case "a ":
        modifications.add(new Added(line.substring(2)));
        break;
      case "m ":
        modifications.add(new Modified(line.substring(2)));
        break;
      case "r ":
        modifications.add(new Removed(line.substring(2)));
        break;
      case "c ":
        String sourceTarget = line.substring(2);
        int divider = sourceTarget.indexOf('\0');
        String source = sourceTarget.substring(0, divider);
        String target = sourceTarget.substring(divider + 1);
        modifications.remove(new Added(target));
        if (modifications.remove(new Removed(source))) {
          modifications.add(new Renamed(source, target));
        } else {
          modifications.add(new Copied(source, target));
        }
        break;
      default:
        // nothing to do
    }
  }

  Collection<Modification> getModifications() {
    return modifications;
  }
}

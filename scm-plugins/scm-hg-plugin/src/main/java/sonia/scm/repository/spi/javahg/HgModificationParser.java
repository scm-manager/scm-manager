/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

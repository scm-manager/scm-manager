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

package sonia.scm.repository;

import com.google.common.collect.ImmutableSet;
import lombok.Value;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Value
public class Contributor implements Serializable {

  public static final String CO_AUTHORED_BY = "Co-authored-by";
  public static final String COMMITTED_BY = "Committed-by";
  public static final String REVIEWED_BY = "Reviewed-by";
  public static final String SIGNED_OFF_BY = "Signed-off-by";

  private static final Collection<String> SUPPORTED_CONTRIBUTOR_TYPES = ImmutableSet.of(CO_AUTHORED_BY, REVIEWED_BY, SIGNED_OFF_BY, COMMITTED_BY);
  private static final Pattern CONTRIBUTOR_PATTERN = Pattern.compile("^([\\w-]*):\\W*(.*)\\W+<(.*)>\\W*$");

  private String type;
  private Person person;

  static Optional<Contributor> fromCommitLine(String line) {
    Matcher matcher = CONTRIBUTOR_PATTERN.matcher(line);
    if (matcher.matches()) {
      String type = matcher.group(1);
      String name = matcher.group(2);
      String mail = matcher.group(3);
      if (SUPPORTED_CONTRIBUTOR_TYPES.contains(type)) {
        return of(new Contributor(type, new Person(name, mail)));
      }
    }
    return empty();
  }

  public String toCommitLine() {
    return String.format("%s: %s <%s>", type, person.getName(), person.getMail());
  }
}

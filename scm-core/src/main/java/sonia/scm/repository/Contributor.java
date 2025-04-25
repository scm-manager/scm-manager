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

import com.google.common.collect.ImmutableSet;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

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
  private Instant time;

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

  public Contributor(String type, Person person) {
    this(type, person, null);
  }

  public Contributor(String type, Person person, Instant time) {
    this.type = type;
    this.person = person;
    this.time = time;
  }

  public Optional<Instant> getTime() {
    return ofNullable(time);
  }

  public String toCommitLine() {
    return String.format("%s: %s <%s>", type, person.getName(), person.getMail());
  }
}

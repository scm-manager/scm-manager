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

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@EqualsAndHashCode
@ToString
@Getter
public class Modifications implements Serializable {

  private static final long serialVersionUID = -8902033326668658140L;

  private final String revision;
  private final String baseRevision;
  private final Collection<Modification> modifications;

  public Modifications(String revision, Modification... modifications) {
    this(revision, asList(modifications));
  }

  public Modifications(String revision, Collection<Modification> modifications) {
    this(null, revision, modifications);
  }

  /**
   * @since 2.23.0
   */
  public Modifications(String baseRevision, String revision, Collection<Modification> modifications) {
    this.baseRevision = baseRevision;
    this.revision = revision;
    this.modifications = ImmutableList.copyOf(modifications);
  }

  /**
   * If these modifications are not related to a single revision but represent the
   * modifications between two revisions, this gives the base revision.
   *
   * @since 2.23.0
   */
  public Optional<String> getBaseRevision() {
    return ofNullable(baseRevision);
  }

  public List<String> getEffectedPaths() {
    return effectedPathsStream().collect(toList());
  }

  public Stream<String> effectedPathsStream() {
    return modifications.stream().flatMap(Modification::getEffectedPaths);
  }

  public List<Added> getAdded() {
    return modifications.stream()
      .filter(m -> m instanceof Added)
      .map(m -> (Added) m)
      .collect(toList());
  }

  public List<Removed> getRemoved() {
    return modifications.stream()
      .filter(m -> m instanceof Removed)
      .map(m -> (Removed) m)
      .collect(toList());
  }

  public List<Modified> getModified() {
    return modifications.stream()
      .filter(m -> m instanceof Modified)
      .map(m -> (Modified) m)
      .collect(toList());
  }

  public List<Renamed> getRenamed() {
    return modifications.stream()
      .filter(m -> m instanceof Renamed)
      .map(m -> (Renamed) m)
      .collect(toList());
  }

  public List<Copied> getCopied() {
    return modifications.stream()
      .filter(m -> m instanceof Copied)
      .map(m -> (Copied) m)
      .collect(toList());
  }
}

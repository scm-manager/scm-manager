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

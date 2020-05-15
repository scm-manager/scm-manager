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
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@EqualsAndHashCode
@ToString
@Getter
public class Modifications implements Serializable {

  private static final long serialVersionUID = -8902033326668658140L;

  private final String revision;
  private final Collection<Modification> modifications;

  public Modifications(String revision, Modification... modifications) {
    this(revision, asList(modifications));
  }

  public Modifications(String revision, Collection<Modification> modifications) {
    this.revision = revision;
    this.modifications = ImmutableList.copyOf(modifications);
  }

  public List<String> getEffectedPaths() {
    return effectedPathsStream().collect(toList());
  }

  public Stream<String> effectedPathsStream() {
    return modifications.stream().flatMap(Modification::getEffectedPaths);
  }

  public List<Modification.Added> getAdded() {
    return modifications.stream()
      .filter(m -> m instanceof Modification.Added)
      .map(m -> (Modification.Added) m)
      .collect(toList());
  }

  public List<Modification.Removed> getRemoved() {
    return modifications.stream()
      .filter(m -> m instanceof Modification.Removed)
      .map(m -> (Modification.Removed) m)
      .collect(toList());
  }

  public List<Modification.Modified> getModified() {
    return modifications.stream()
      .filter(m -> m instanceof Modification.Modified)
      .map(m -> (Modification.Modified) m)
      .collect(toList());
  }

  public List<Modification.Renamed> getRenamed() {
    return modifications.stream()
      .filter(m -> m instanceof Modification.Renamed)
      .map(m -> (Modification.Renamed) m)
      .collect(toList());
  }

  public List<Modification.Copied> getCopied() {
    return modifications.stream()
      .filter(m -> m instanceof Modification.Copied)
      .map(m -> (Modification.Copied) m)
      .collect(toList());
  }
}

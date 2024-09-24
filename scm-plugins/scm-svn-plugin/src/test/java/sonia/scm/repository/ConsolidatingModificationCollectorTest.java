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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConsolidatingModificationCollectorTest {

  @Test
  void shouldKeepIndependentChanges() {
    Collection<Modification> consolidated =
      Stream.of(
        new Added("added"),
        new Removed("removed"),
        new Modified("modified")
      ).collect(new ConsolidatingModificationCollector());

    assertThat(consolidated)
      .extracting("class")
      .containsExactlyInAnyOrder(Added.class, Removed.class, Modified.class);
  }

  @Test
  void shouldNotListAddedFileIfRemovedLaterOn() {
    Collection<Modification> consolidated =
      Stream.of(
        new Added("file"),
        new Removed("file")
      ).collect(new ConsolidatingModificationCollector());

    assertThat(consolidated)
      .isEmpty();
  }

  @Test
  void shouldReplaceModificationWithRemove() {
    Collection<Modification> consolidated =
      Stream.of(
        new Modified("file"),
        new Removed("file")
      ).collect(new ConsolidatingModificationCollector());

    assertThat(consolidated)
      .extracting("class")
      .containsExactly(Removed.class);
  }

  @Test
  void shouldReplaceCopyWithAdd() {
    Collection<Modification> consolidated =
      Stream.of(
        new Copied("source", "target")
      ).collect(new ConsolidatingModificationCollector());

    assertThat(consolidated)
      .extracting("class")
      .containsExactly(Added.class);
    assertThat(consolidated)
      .extracting("path")
      .containsExactly("target");
  }

  @Test
  void shouldReplaceRenameWithAddAndRemove() {
    Collection<Modification> consolidated =
      Stream.of(
        new Renamed("source", "target")
      ).collect(new ConsolidatingModificationCollector());

    assertThat(consolidated)
      .extracting("class")
      .containsExactlyInAnyOrder(Added.class, Removed.class);
    assertThat(consolidated)
      .extracting("path")
      .containsExactlyInAnyOrder("source", "target");
  }

  @Test
  void shouldNotReplaceAddWithModify() {
    Collection<Modification> consolidated =
      Stream.of(
        new Added("file"),
        new Modified("file")
      ).collect(new ConsolidatingModificationCollector());

    assertThat(consolidated)
      .extracting("class")
      .containsExactlyInAnyOrder(Added.class);
  }

  @Test
  void shouldReplaceAddWithModifyIfRemovedBefore() {
    Collection<Modification> consolidated =
      Stream.of(
        new Removed("file"),
        new Added("file")
      ).collect(new ConsolidatingModificationCollector());

    assertThat(consolidated)
      .extracting("class")
      .containsExactlyInAnyOrder(Modified.class);
  }
}

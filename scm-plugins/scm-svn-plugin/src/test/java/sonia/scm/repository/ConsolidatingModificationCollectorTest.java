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

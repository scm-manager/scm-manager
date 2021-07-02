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

package sonia.scm.search;

import org.junit.jupiter.api.Test;
import sonia.scm.ModelObject;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdTest {

  @Test
  void shouldCreateIdFromPrimary() {
    Id id = Id.of("one");
    assertThat(id.getValue()).isEqualTo("one");
  }

  @Test
  void shouldCreateIdWithoutRepository() {
    Id id = Id.of("one");
    assertThat(id.getRepository()).isEmpty();
  }

  @Test
  void shouldFailWithoutPrimaryValue() {
    assertThrows(IllegalArgumentException.class, () -> Id.of((String) null));
  }

  @Test
  void shouldFailWithEmptyPrimaryValue() {
    assertThrows(IllegalArgumentException.class, () -> Id.of(""));
  }

  @Test
  void shouldCreateCombinedValue() {
    Id id = Id.of("one", "two", "three");
    assertThat(id.getValue()).isEqualTo("one:two:three");
  }

  @Test
  void shouldAddRepositoryId() {
    Id id = Id.of("one").withRepository("4211");
    assertThat(id.getRepository()).contains("4211");
  }

  @Test
  void shouldAddRepository() {
    Repository repository = new Repository();
    repository.setId("4211");
    Id id = Id.of("one").withRepository(repository);
    assertThat(id.getRepository()).contains("4211");
  }

  @Test
  void shouldCreateIdFromRepository() {
    Repository repository = new Repository();
    repository.setId("4211");
    Id id = Id.of(repository);
    assertThat(id.getRepository()).contains("4211");
  }

  @Test
  void shouldFailWithoutRepository() {
    Id id = Id.of("one");
    assertThrows(IllegalArgumentException.class, () -> id.withRepository((Repository) null));
  }

  @Test
  void shouldFailWithoutRepositoryId() {
    Id id = Id.of("one");
    assertThrows(IllegalArgumentException.class, () -> id.withRepository((String) null));
  }

  @Test
  void shouldFailWithEmptyRepositoryId() {
    Id id = Id.of("one");
    assertThrows(IllegalArgumentException.class, () -> id.withRepository((String) null));
  }

  @Test
  void shouldCreateIdFromModelObject() {
    Id id = Id.of(new User("trillian"));
    assertThat(id.getValue()).isEqualTo("trillian");
  }

  @Test
  void shouldFailWithoutModelObject() {
    assertThrows(IllegalArgumentException.class, () -> Id.of((ModelObject) null));
  }

}

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
import sonia.scm.group.Group;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdTest {

  @Test
  void shouldCreateSimpleId() {
    Id<Repository> id = Id.of(Repository.class, "42");
    assertThat(id.getMainType()).isEqualTo(Repository.class);
    assertThat(id.getMainId()).isEqualTo("42");
    assertThat(id.getOthers()).isEmpty();
  }

  @Test
  void shouldFailWithNullType() {
    assertThrows(IllegalArgumentException.class, () -> Id.of(null, "42"));
  }

  @Test
  void shouldFailWithEmptyId() {
    assertThrows(IllegalArgumentException.class, () -> Id.of(Repository.class, ""));
  }

  @Test
  void shouldFailWithNullId() {
    assertThrows(IllegalArgumentException.class, () -> Id.of(Repository.class, (String) null));
  }

  @Test
  void shouldFailWithNullIdObject() {
    assertThrows(IllegalArgumentException.class, () -> Id.of(Repository.class, (ModelObject) null));
  }

  @Test
  void shouldCreateSimpleFromModelObject() {
    Repository repository = new Repository();
    repository.setId("42");
    Id<Repository> id = Id.of(Repository.class, repository);
    assertThat(id.getMainType()).isEqualTo(Repository.class);
    assertThat(id.getMainId()).isEqualTo("42");
    assertThat(id.getOthers()).isEmpty();
  }

  @Test
  void shouldCreateIdWithOneOther() {
    Id<User> id = Id.of(User.class, "trillian").and(Group.class, "hog");
    assertThat(id.getMainType()).isEqualTo(User.class);
    assertThat(id.getMainId()).isEqualTo("trillian");
    assertThat(id.getOthers()).containsEntry(Group.class, "hog");
  }

  @Test
  void shouldCreateIdWithOtherFromModelObject() {
    Group group = new Group("xml", "hog");
    Id<User> id = Id.of(User.class, "trillian").and(Group.class, group);
    assertThat(id.getMainType()).isEqualTo(User.class);
    assertThat(id.getMainId()).isEqualTo("trillian");
    assertThat(id.getOthers()).containsEntry(Group.class, "hog");
  }

  @Test
  void shouldCreateIdWithOthers() {
    Id<User> id = Id.of(User.class, "trillian")
      .and(Group.class, "hog")
      .and(Repository.class, "heart-of-gold");

    assertThat(id.getMainType()).isEqualTo(User.class);
    assertThat(id.getMainId()).isEqualTo("trillian");
    assertThat(id.getOthers())
      .containsEntry(Group.class, "hog")
      .containsEntry(Repository.class, "heart-of-gold");
  }

  @Test
  void shouldFailIfOtherTypeIsNull() {
    Id<User> id = Id.of(User.class, "trillian");
    assertThrows(IllegalArgumentException.class, () -> id.and(null, "hog"));
  }

  @Test
  void shouldFailIfOtherIdIsNull() {
    Id<User> id = Id.of(User.class, "trillian");
    assertThrows(IllegalArgumentException.class, () -> id.and(Group.class, (String) null));
  }

  @Test
  void shouldFailIfOtherIdIsEmpty() {
    Id<User> id = Id.of(User.class, "trillian");
    assertThrows(IllegalArgumentException.class, () -> id.and(Group.class, ""));
  }

  @Test
  void shouldFailIfOtherIdObjectIsNull() {
    Id<User> id = Id.of(User.class, "trillian");
    assertThrows(IllegalArgumentException.class, () -> id.and(Group.class, (ModelObject) null));
  }

}

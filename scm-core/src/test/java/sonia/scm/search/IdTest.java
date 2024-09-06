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
    Repository repository = new Repository();
    repository.setId("heart-of-gold");

    Id<User> id = Id.of(User.class, "trillian")
      .and(Group.class, "hog")
      .and(repository);

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

  @Test
  void shouldFailIfRepositoryIsNull() {
    Id<User> id = Id.of(User.class, "trillian");
    assertThrows(IllegalArgumentException.class, () -> id.and(null));
  }

}

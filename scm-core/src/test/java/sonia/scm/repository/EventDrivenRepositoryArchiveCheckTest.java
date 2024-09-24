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
import sonia.scm.HandlerEventType;

import java.util.Collections;

import static java.util.Collections.singleton;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.EventDrivenRepositoryArchiveCheck.setAsArchived;

class EventDrivenRepositoryArchiveCheckTest {

  private static final Repository NORMAL_REPOSITORY = new Repository("hog", "git", "hitchhiker", "hog");
  private static final Repository ARCHIVED_REPOSITORY = new Repository("archived_hog", "git", "hitchhiker", "hog");
  static {
    ARCHIVED_REPOSITORY.setArchived(true);
    EventDrivenRepositoryArchiveCheck.setAsArchived(ARCHIVED_REPOSITORY.getId());
  }

  EventDrivenRepositoryArchiveCheck check = new EventDrivenRepositoryArchiveCheck();

  @Test
  void shouldBeNotArchivedByDefault() {
    assertThat(check.isArchived("hog")).isFalse();
  }

  @Test
  void shouldBeArchivedAfterFlagHasBeenSet() {
    check.updateListener(new RepositoryModificationEvent(HandlerEventType.MODIFY, ARCHIVED_REPOSITORY, NORMAL_REPOSITORY));
    assertThat(check.isArchived("archived_hog")).isTrue();
  }

  @Test
  void shouldNotBeArchivedAfterFlagHasBeenRemoved() {
    setAsArchived("hog");
    check.updateListener(new RepositoryModificationEvent(HandlerEventType.MODIFY, NORMAL_REPOSITORY, ARCHIVED_REPOSITORY));
    assertThat(check.isArchived("hog")).isFalse();
  }

  @Test
  void shouldBeInitialized() {
    RepositoryDAO repositoryDAO = mock(RepositoryDAO.class);
    when(repositoryDAO.getAll()).thenReturn(singleton(ARCHIVED_REPOSITORY));

    new EventDrivenRepositoryArchiveCheckInitializer(repositoryDAO).init(null);

    assertThat(check.isArchived("archived_hog")).isTrue();
  }

  @Test
  void shouldBeReadOnly() {
    boolean readOnly = check.isArchived(ARCHIVED_REPOSITORY);
    assertThat(readOnly).isTrue();
  }

  @Test
  void shouldNotBeReadOnly() {
    boolean readOnly = check.isArchived(NORMAL_REPOSITORY);
    assertThat(readOnly).isFalse();
  }
}

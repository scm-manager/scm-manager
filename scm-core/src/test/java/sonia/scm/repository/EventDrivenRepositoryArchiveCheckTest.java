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

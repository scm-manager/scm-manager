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

package sonia.scm.repository.spi;


import com.google.common.collect.Iterables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Modifications;

import java.time.Instant;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SvnLogCommandTest extends AbstractSvnCommandTestBase {

  @Test
  public void testGetAll() {
    ChangesetPagingResult result =
      createCommand().getChangesets(new LogCommandRequest());

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(5);
    assertThat(result.getChangesets()).hasSize(5);
  }

  @Test
  public void shouldNotReturnChangesetWithIdZero() {
    ChangesetPagingResult result = createCommand().getChangesets(new LogCommandRequest());
    boolean found = StreamSupport.stream(result.spliterator(), false).anyMatch(c -> "0".equals(c.getId()));

    assertThat(found).isFalse();
  }

  @Test
  public void shouldThrowIllegalArgumentExceptionForChangesetZero() {
    assertThatThrownBy(() -> createCommand().getChangeset("0", new LogCommandRequest()))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldNotReturnChangesetZeroAsParent() {
    Changeset changeset = createCommand().getChangeset("1", new LogCommandRequest());
    assertThat(changeset.getParents()).isEmpty();
  }

  @Test
  public void shouldAppendParentChangeset() {
    Changeset changeset = createCommand().getChangeset("2", new LogCommandRequest());
    assertThat(changeset.getParents()).hasSize(1);
    assertThat(changeset.getParents().get(0)).isEqualTo("1");
  }

  @Test
  public void testShouldStartWithRevisionOne() {
    ChangesetPagingResult result = createCommand().getChangesets(new LogCommandRequest());
    Changeset first = Iterables.getLast(result);
    assertThat(first.getId()).isEqualTo("1");
  }

  @Test
  public void testGetAllByPath() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPath("a.txt");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(3);
    assertThat(result.getChangesets()).hasSize(3);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of("5", "3", "1"));
  }

  @Test
  public void testGetAllWithLimit() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(5);
    assertThat(result.getChangesets()).hasSize(2);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of("5", "4"));
  }

  @Test
  public void testGetAllWithPaging() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(1);
    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(5);
    assertThat(result.getChangesets()).hasSize(2);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of("4", "3"));
  }

  @Test
  public void testGetCommit() {
    Changeset c = createCommand().getChangeset("3", null);

    assertThat(c).isNotNull();
    assertThat(c.getId()).isEqualTo("3");
    assertThat(c.getDescription()).isEqualTo("remove b and modified a");
    assertThat(c.getAuthor().getName()).isEqualTo("perfect");
    assertThat(c.getAuthor().getMail()).isNull();
    checkDate(c.getDate());

    SvnModificationsCommand modificationsCommand = new SvnModificationsCommand(createContext());
    Modifications modifications = modificationsCommand.getModifications("3");

    assertThat(modifications).isNotNull();
    assertThat(modifications.getModified()).hasSize(1);
    assertThat(modifications.getRemoved()).hasSize(1);
    assertThat(modifications.getAdded()).isEmpty();
    assertThat(modifications.getModified().get(0).getPath()).isEqualTo("a.txt");
    assertThat(modifications.getRemoved().get(0).getPath()).isEqualTo("b.txt");
  }

  @Test
  public void testGetRange() {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("2");
    request.setEndChangeset("1");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(2);
    assertThat(result.getChangesets()).hasSize(2);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of("2", "1"));
  }

  @Test
  public void shouldGetCorrectHeadRevision() {
    Changeset changeset = createCommand().getChangeset("head", new LogCommandRequest());
    assertThat(changeset.getId()).isEqualTo("5");
  }

  private SvnLogCommand createCommand() {
    return new SvnLogCommand(createContext());
  }

  @Test
  public void shouldFindChangesetsContainingMessage() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPartOfMessage("a.TXT");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getChangesets()).hasSize(1);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of("1"));
  }

  @Test
  public void shouldFindChangesetsByAuthorName() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPerson("pho");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "5"
      ));
  }

  @Test
  public void shouldFindChangesetsBeforeInstant() {
    LogCommandRequest request = new LogCommandRequest();

    //13.06.2012 11:14:00Z
    request.setBefore(Instant.ofEpochMilli(1339575660000L));

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "3", "2", "1"
      ));
  }

  @Test
  public void shouldFindChangesetsAfterInstant() {
    LogCommandRequest request = new LogCommandRequest();

    //13.06.2012 11:14:00Z
    request.setAfter(Instant.ofEpochMilli(1339575660000L));

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "5", "4"
      ));
  }

  @Nested
  class LastPageWithoutFilters {

    @Test
    public void shouldReturnLastPage() {
      LogCommandRequest request = new LogCommandRequest();

      request.setPagingStart(4);
      request.setPagingLimit(2);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "1"
        ));
    }

    @Test
    public void shouldReturnNoResultsBecauseUserIsAfterLastPage() {
      LogCommandRequest request = new LogCommandRequest();

      request.setPagingStart(5);
      request.setPagingLimit(3);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
        ));
    }

    @Test
    public void shouldReturnLastCompletePageBecauseStartIsNegative() {
      LogCommandRequest request = new LogCommandRequest();

      request.setPagingStart(-1);
      request.setPagingLimit(1);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "1"
        ));
    }

    @Test
    public void shouldReturnLastPageWithNegativeStartAndIncompleteLastPage() {
      LogCommandRequest request = new LogCommandRequest();

      request.setPagingStart(-1);
      request.setPagingLimit(3);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "2",
          "1"
        ));
    }

    @Test
    public void shouldReturnLastPageWithHigherLimitThanTotal() {
      LogCommandRequest request = new LogCommandRequest();

      request.setPagingStart(-1);
      request.setPagingLimit(100);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "5",
          "4",
          "3",
          "2",
          "1"
        ));
    }

    @Test
    public void shouldReturnLastPageWithEveryCommitBecauseLimitIsNegative() {
      LogCommandRequest request = new LogCommandRequest();

      request.setPagingStart(-1);
      request.setPagingLimit(-1);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "5",
          "4",
          "3",
          "2",
          "1"
        ));
    }
  }

  @Nested
  class LastPageWithFilters {
    private LogCommandRequest request;

    @BeforeEach
    void setup() {
      request = new LogCommandRequest();
      request.setBefore(Instant.MAX);
    }

    @Test
    public void shouldReturnLastPageWithoutStartAndEndChangeset() {
      request.setPagingStart(4);
      request.setPagingLimit(2);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "1"
        ));
    }

    @Test
    public void shouldReturnLastPageWithStartChangeset() {
      request.setStartChangeset("2");
      request.setPagingStart(1);
      request.setPagingLimit(1);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(2);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "1"
        ));
    }

    @Test
    public void shouldReturnLastPageWithEndChangeset() {
      request.setEndChangeset("2");
      request.setPagingStart(2);
      request.setPagingLimit(2);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(4);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "3",
          "2"
        ));
    }

    @Test
    public void shouldReturnNoResultsBecauseUserIsAfterLastPage() {
      request.setPagingStart(5);
      request.setPagingLimit(3);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
        ));
    }

    @Test
    public void shouldReturnLastCompletePageBecauseStartIsNegative() {
      request.setPagingStart(-1);
      request.setPagingLimit(1);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "1"
        ));
    }

    @Test
    public void shouldReturnLastPageWithNegativeStartAndIncompleteLastPage() {
      request.setPagingStart(-1);
      request.setPagingLimit(3);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "2",
          "1"
        ));
    }

    @Test
    public void shouldReturnLastPageWithHigherLimitThanTotal() {
      request.setPagingStart(-1);
      request.setPagingLimit(100);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "5",
          "4",
          "3",
          "2",
          "1"
        ));
    }

    @Test
    public void shouldReturnLastPageWithEveryCommitBecauseLimitIsNegative() {
      request.setPagingStart(-1);
      request.setPagingLimit(-1);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "5",
          "4",
          "3",
          "2",
          "1"
        ));
    }
  }
}

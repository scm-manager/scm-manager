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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Added;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Modifications;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class HgLogCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void testGetAll() {
    ChangesetPagingResult result =
      createCommand().getChangesets(new LogCommandRequest());

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(5);
    assertThat(result.getChangesets()).hasSize(5);
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
      .isEqualTo(List.of(
        "2baab8e80280ef05a9aa76c49c76feca2872afb7",
        "79b6baf49711ae675568e0698d730b97ef13e84a",
        "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
      ));
  }

  @Test
  public void testGetDefaultBranchInfo() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPath("a.txt");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getChangesets().get(0).getBranches())
      .isEqualTo(List.of("default"));
  }

  @Test
  public void testGetAllWithLimit() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(5);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "2baab8e80280ef05a9aa76c49c76feca2872afb7",
        "542bf4893dd2ff58a0eb719551d75ddeb919608b"
      ));
  }

  @Test
  public void testGetAllWithPaging() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(1);
    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(5);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "542bf4893dd2ff58a0eb719551d75ddeb919608b",
        "79b6baf49711ae675568e0698d730b97ef13e84a"
      ));
  }

  @Test
  public void testGetCommit() throws IOException {
    HgLogCommand command = createCommand();
    String revision = "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427";
    Changeset c = command.getChangeset(revision, null);

    assertThat(c).isNotNull();
    assertThat(c.getId()).isEqualTo(revision);
    assertThat(c.getDescription()).isEqualTo("added a and b files");
    checkDate(c.getDate());
    assertThat(c.getAuthor().getName()).isEqualTo("Douglas Adams");
    assertThat(c.getAuthor().getMail()).isEqualTo("douglas.adams@hitchhiker.com");

    ModificationsCommand modificationsCommand = new HgModificationsCommand(cmdContext);
    Modifications modifications = modificationsCommand.getModifications(revision);

    assertThat(modifications).isNotNull();
    assertThat(modifications.getModified()).isEmpty();
    assertThat(modifications.getRemoved()).isEmpty();
    assertThat(modifications.getAdded().stream().map(Added::getPath)).isEqualTo(List.of("a.txt", "b.txt"));
  }

  @Test
  public void testGetRange() {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("3049df33fdbb");
    request.setEndChangeset("a9bacaf1b7fa");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(2);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "3049df33fdbbded08b707bac3eccd0f7b453c58b",
        "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
      ));
  }

  private HgLogCommand createCommand() {
    return new HgLogCommand(cmdContext);
  }

  @Test
  public void shouldFindChangesetsContainingMessage() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPartOfMessage("FiLE");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "542bf4893dd2ff58a0eb719551d75ddeb919608b",
        "79b6baf49711ae675568e0698d730b97ef13e84a",
        "3049df33fdbbded08b707bac3eccd0f7b453c58b",
        "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
      ));
  }

  @Test
  public void shouldFindChangesetsByAuthorEmail() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPerson("BEEblebrox@hitchhiker");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "2baab8e80280ef05a9aa76c49c76feca2872afb7"
      ));
  }

  @Test
  public void shouldFindChangesetsByAuthorName() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPerson("phod bee");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "2baab8e80280ef05a9aa76c49c76feca2872afb7"
      ));
  }

  @Test
  public void shouldFindChangesetsBeforeInstant() {
    LogCommandRequest request = new LogCommandRequest();

    //13.06.2012 11:14:00Z
    request.setBefore(Instant.ofEpochMilli(1339586160000L));

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "3049df33fdbbded08b707bac3eccd0f7b453c58b",
        "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
      ));
  }

  @Test
  public void shouldFindChangesetsAfterInstant() {
    LogCommandRequest request = new LogCommandRequest();

    //13.06.2012 11:14:00Z
    request.setAfter(Instant.ofEpochMilli(1339586160000L));

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "2baab8e80280ef05a9aa76c49c76feca2872afb7",
        "542bf4893dd2ff58a0eb719551d75ddeb919608b",
        "79b6baf49711ae675568e0698d730b97ef13e84a"
      ));
  }

  @Nested
  class LastPageWithoutFilters {

    @Test
    public void shouldReturnLastPageWithoutStartAndEndChangeset() {
      LogCommandRequest request = new LogCommandRequest();

      request.setPagingStart(4);
      request.setPagingLimit(2);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(5);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
        ));
    }

    @Test
    public void shouldReturnLastPageWithStartChangeset() {
      LogCommandRequest request = new LogCommandRequest();

      request.setStartChangeset("3049df33fdbbded08b707bac3eccd0f7b453c58b");
      request.setPagingStart(1);
      request.setPagingLimit(1);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(2);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
        ));
    }

    @Test
    public void shouldReturnLastPageWithEndChangeset() {
      LogCommandRequest request = new LogCommandRequest();

      request.setEndChangeset("3049df33fdbbded08b707bac3eccd0f7b453c58b");
      request.setPagingStart(2);
      request.setPagingLimit(2);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(4);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "79b6baf49711ae675568e0698d730b97ef13e84a",
          "3049df33fdbbded08b707bac3eccd0f7b453c58b"
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
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
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
          "3049df33fdbbded08b707bac3eccd0f7b453c58b",
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
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
          "2baab8e80280ef05a9aa76c49c76feca2872afb7",
          "542bf4893dd2ff58a0eb719551d75ddeb919608b",
          "79b6baf49711ae675568e0698d730b97ef13e84a",
          "3049df33fdbbded08b707bac3eccd0f7b453c58b",
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
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
          "2baab8e80280ef05a9aa76c49c76feca2872afb7",
          "542bf4893dd2ff58a0eb719551d75ddeb919608b",
          "79b6baf49711ae675568e0698d730b97ef13e84a",
          "3049df33fdbbded08b707bac3eccd0f7b453c58b",
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
        ));
    }
  }

  @Nested
  class LastPageWithFilters {

    private LogCommandRequest request;

    @BeforeEach
    void setup() {
      request = new LogCommandRequest();
      request.setBranch("default");
    }

    @Test
    public void shouldReturnLastPageWithoutStartAndEndChangeset() {
      request.setPagingStart(3);
      request.setPagingLimit(2);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(4);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
        ));
    }

    @Test
    public void shouldReturnLastPageWithStartChangeset() {
      request.setStartChangeset("3049df33fdbbded08b707bac3eccd0f7b453c58b");
      request.setPagingStart(1);
      request.setPagingLimit(1);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(2);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
        ));
    }

    @Test
    public void shouldReturnLastPageWithEndChangeset() {
      request.setEndChangeset("3049df33fdbbded08b707bac3eccd0f7b453c58b");
      request.setPagingStart(2);
      request.setPagingLimit(2);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(3);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "3049df33fdbbded08b707bac3eccd0f7b453c58b"
        ));
    }

    @Test
    public void shouldReturnNoResultsBecauseUserIsAfterLastPage() {
      request.setPagingStart(5);
      request.setPagingLimit(3);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(4);
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
      assertThat(result.getTotal()).hasValue(4);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
        ));
    }

    @Test
    public void shouldReturnLastPageWithNegativeStartAndIncompleteLastPage() {
      request.setPagingStart(-1);
      request.setPagingLimit(3);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(4);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
        ));
    }

    @Test
    public void shouldReturnLastPageWithHigherLimitThanTotal() {
      request.setPagingStart(-1);
      request.setPagingLimit(100);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(4);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "2baab8e80280ef05a9aa76c49c76feca2872afb7",
          "542bf4893dd2ff58a0eb719551d75ddeb919608b",
          "3049df33fdbbded08b707bac3eccd0f7b453c58b",
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
        ));
    }

    @Test
    public void shouldReturnLastPageWithEveryCommitBecauseLimitIsNegative() {
      request.setPagingStart(-1);
      request.setPagingLimit(-1);

      ChangesetPagingResult result = createCommand().getChangesets(request);

      assertThat(result).isNotNull();
      assertThat(result.getTotal()).hasValue(4);
      assertThat(result.isHasMore()).isFalse();
      assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
        .isEqualTo(List.of(
          "2baab8e80280ef05a9aa76c49c76feca2872afb7",
          "542bf4893dd2ff58a0eb719551d75ddeb919608b",
          "3049df33fdbbded08b707bac3eccd0f7b453c58b",
          "a9bacaf1b7fa0cebfca71fed4e59ed69a6319427"
        ));
    }
  }
}

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

import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.CODE_WITHOUT_ADDITIONAL_MESSAGES;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.CODE_WITH_ADDITIONAL_MESSAGES;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.forMessage;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.withPattern;

class IntegrateChangesFromWorkdirExceptionTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "hitchhiker", "hog");

  @Test
  void shouldExtractMessagesWithDefaultPrefix() {
    IntegrateChangesFromWorkdirException exception =
      forMessage(REPOSITORY, "prefix [SCM] line 1\nprefix [SCM] line 2\nirrelevant line\n");

    assertThat(exception.getAdditionalMessages())
      .extracting("message")
      .containsExactly("line 1", "line 2");
    assertThat(exception.getCode()).isEqualTo(CODE_WITH_ADDITIONAL_MESSAGES);
  }

  @Test
  void shouldExtractMessagesWithCustomPattern() {
    IntegrateChangesFromWorkdirException exception =
      withPattern(Pattern.compile("-custom- (.*)"))
      .forMessage(REPOSITORY, "to be ignored\n-custom- line\n");

    assertThat(exception.getAdditionalMessages())
      .extracting("message")
      .containsExactly("line");
    assertThat(exception.getCode()).isEqualTo(CODE_WITH_ADDITIONAL_MESSAGES);
  }

  @Test
  void shouldCreateSpecialMessageForMissingAdditionalMessages() {
    IntegrateChangesFromWorkdirException exception =
      forMessage(REPOSITORY, "There is no message");

    assertThat(exception.getAdditionalMessages()).isEmpty();
    assertThat(exception.getCode()).isEqualTo(CODE_WITHOUT_ADDITIONAL_MESSAGES);
  }
}

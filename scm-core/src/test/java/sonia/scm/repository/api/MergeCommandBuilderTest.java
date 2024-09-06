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

package sonia.scm.repository.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.spi.MergeCommand;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.EMail;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MergeCommandBuilderTest {

  @Mock
  private MergeCommand mergeCommand;
  @Mock
  private EMail eMail;

  @InjectMocks
  private MergeCommandBuilder mergeCommandBuilder;

  @BeforeEach
  void prepareCommandBuilder() {
    mergeCommandBuilder.setBranchToMerge("irrelevant");
    mergeCommandBuilder.setTargetBranch("irrelevant");
  }

  @Test
  void shouldUseMailAddressFromEMailFallback() {
    User user = new User("dent", "Arthur Dent", null);
    DisplayUser author = DisplayUser.from(user);
    when(eMail.getMailOrFallback(author)).thenReturn("dent@hitchhiker.com");
    mergeCommandBuilder.setAuthor(author);

    mergeCommandBuilder.executeMerge();

    verify(mergeCommand).merge(argThat(mergeCommandRequest -> {
      assertThat(mergeCommandRequest.getAuthor().getMail()).isEqualTo("dent@hitchhiker.com");
      return true;
    }));
  }
}

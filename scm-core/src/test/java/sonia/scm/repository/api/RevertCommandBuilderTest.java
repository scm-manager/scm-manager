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

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.spi.RevertCommand;
import sonia.scm.repository.spi.RevertCommandRequest;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.EMail;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevertCommandBuilderTest {

  @Mock
  private RevertCommand revertCommand;
  @Mock
  private EMail eMail;

  @InjectMocks
  private RevertCommandBuilder revertCommandBuilder;

  @BeforeEach
  void prepareCommandBuilder() {
    revertCommandBuilder.setRevision("irrelevant");
  }

  @Test
  void shouldUseMailAddressFromEMailFallback() {
    User user = new User("dent", "Arthur Dent", null);
    DisplayUser author = DisplayUser.from(user);
    when(eMail.getMailOrFallback(author)).thenReturn("dent@hitchhiker.com");
    revertCommandBuilder.setAuthor(author);
    revertCommandBuilder.execute();

    verify(revertCommand).revert(argThat(revertCommandRequest -> {
      assertThat(revertCommandRequest.getAuthor().getMail()).isEqualTo("dent@hitchhiker.com");
      return true;
    }));
  }

  @Test
  void shouldSetAuthorFromShiroSubjectIfNotSet() {
    User user = new User("dent", "Arthur Dent", null);DisplayUser author = DisplayUser.from(user);
    when(eMail.getMailOrFallback(author)).thenReturn("dent@hitchhiker.com");
    mockLoggedInUser(user);
    revertCommandBuilder.execute();
    RevertCommandRequest request = revertCommandBuilder.getRequest();

    assertThat(request.getAuthor().getName()).isEqualTo("Arthur Dent");
    assertThat(request.getAuthor().getMail()).isEqualTo("dent@hitchhiker.com");

    mockLogout();
  }

  @Test
  void shouldSetAllFieldsInRequest() {
    User user = new User("dent", "Arthur Dent", null);
    DisplayUser author = DisplayUser.from(user);
    when(eMail.getMailOrFallback(author)).thenReturn("dent@hitchhiker.com");
    revertCommandBuilder.setAuthor(author);
    revertCommandBuilder.setBranch("someBranch");
    revertCommandBuilder.setMessage("someMessage");

    RevertCommandRequest request = revertCommandBuilder.getRequest();

    assertThat(request.getAuthor().getName()).isEqualTo(author.getDisplayName());
    assertThat(request.getBranch()).contains("someBranch");
    assertThat(request.getMessage()).contains("someMessage");
    assertThat(request.getRevision()).isEqualTo("irrelevant");
  }

  @Test
  void shouldNotExecuteInvalidRequestDueToEmptyBranch() {
    User user = new User("dent", "Arthur Dent", "dent@hitchhiker.com");
    revertCommandBuilder.setAuthor(DisplayUser.from(user));
    revertCommandBuilder.setBranch("");
    assertThatThrownBy(() -> revertCommandBuilder.execute())
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Revert request is invalid, request was: RevertCommandRequest(author=Arthur Dent, revision=irrelevant, sign=true, branch=Optional[], message=Optional.empty)");
  }

  private void mockLoggedInUser(User loggedInUser) {
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
    PrincipalCollection principals = mock(PrincipalCollection.class);
    when(subject.getPrincipals()).thenReturn(principals);
    when(principals.oneByType(User.class)).thenReturn(loggedInUser);
  }

  private void mockLogout() {
    ThreadContext.unbindSubject();
  }
}

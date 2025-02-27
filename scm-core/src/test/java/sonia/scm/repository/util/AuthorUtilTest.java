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

package sonia.scm.repository.util;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Person;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.EMail;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorUtilTest {

  @Mock
  private EMail eMail;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Subject subject;

  @BeforeEach
  void setUpSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldCreateMailAddressFromEmail() {
    User trillian = new User("trillian");

    when(subject.getPrincipals().oneByType(User.class)).thenReturn(trillian);
    when(eMail.getMailOrFallback(DisplayUser.from(trillian))).thenReturn("tricia@hitchhicker.com");
    Command command = new Command(null);
    AuthorUtil.setAuthorIfNotAvailable(command, eMail);

    assertThat(command.getAuthor().getMail()).isEqualTo("tricia@hitchhicker.com");
  }

  @Test
  void shouldUseUsersMailAddressWithoutEMail() {
    User trillian = new User("trillian", "Trillian", "trillian.mcmillan@hitchhiker.com");
    when(subject.getPrincipals().oneByType(User.class)).thenReturn(trillian);

    Command command = new Command(null);
    AuthorUtil.setAuthorIfNotAvailable(command);

    assertThat(command.getAuthor().getMail()).isEqualTo("trillian.mcmillan@hitchhiker.com");
  }

  @Test
  void shouldKeepExistingAuthor() {
    Person person = new Person("Trillian McMillan", "trillian.mcmillian@hitchhiker.com");

    Command command = new Command(person);
    AuthorUtil.setAuthorIfNotAvailable(command);

    assertThat(command.getAuthor()).isSameAs(person);
  }

  public static class Command implements AuthorUtil.CommandWithAuthor {

    private Person person;

    public Command(Person person) {
      this.person = person;
    }

    @Override
    public Person getAuthor() {
      return person;
    }

    @Override
    public void setAuthor(Person person) {
      this.person = person;
    }
  }

}

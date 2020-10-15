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
import sonia.scm.user.EMail;
import sonia.scm.user.User;
import sonia.scm.web.UserAgentParserTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    when(eMail.createFallbackMailAddress(trillian)).thenReturn("tricia@hitchhicker.com");

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

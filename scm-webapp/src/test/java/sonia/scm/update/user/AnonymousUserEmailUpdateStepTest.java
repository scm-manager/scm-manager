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

package sonia.scm.update.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContext;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnonymousUserEmailUpdateStepTest {

  @Mock
  XmlUserDAO userDAO;

  @InjectMocks
  AnonymousUserEmailUpdateStep updateStep;

  @Test
  void shouldDoNothingIfNoAnonymousUser() throws Exception {
    updateStep.doUpdate();

    verify(userDAO, never()).modify(any());
  }

  @Test
  void shouldDoNothingIfAnonymousUserMailWasChanged() throws Exception {
    User anonymous = new User(SCMContext.USER_ANONYMOUS);
    when(userDAO.get("_anonymous")).thenReturn(anonymous);

    updateStep.doUpdate();

    verify(userDAO, never()).modify(any());
  }

  @Test
  void shouldClearAnonymousUserMail() throws Exception {
    User anonymous = new User(SCMContext.USER_ANONYMOUS);
    anonymous.setMail("scm-anonymous@scm-manager.org");
    when(userDAO.get("_anonymous")).thenReturn(anonymous);

    updateStep.doUpdate();

    verify(userDAO).modify(argThat(user -> user.getMail().equals("")));
  }
}

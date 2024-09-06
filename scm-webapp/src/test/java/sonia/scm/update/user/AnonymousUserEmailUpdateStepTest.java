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

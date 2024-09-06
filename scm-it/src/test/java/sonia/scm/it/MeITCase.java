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

package sonia.scm.it;

import org.junit.Before;
import org.junit.Test;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.TestData;

public class MeITCase {

  @Before
  public void init() {
    TestData.cleanup();
  }

  @Test
  public void adminShouldChangeOwnPassword() {
    String newPassword = TestData.USER_SCM_ADMIN + "1";
    // admin change the own password
    ScmRequests.start()
      .requestIndexResource(TestData.USER_SCM_ADMIN, TestData.USER_SCM_ADMIN)
      .requestMe()
      .assertStatusCode(200)
      .requestChangePassword(TestData.USER_SCM_ADMIN, newPassword)
      .assertStatusCode(204);
    // assert password is changed -> login with the new Password than undo changes
    ScmRequests.start()
      .requestIndexResource(TestData.USER_SCM_ADMIN, newPassword)
      .requestMe()
      .assertStatusCode(200)
      .requestChangePassword(newPassword, TestData.USER_SCM_ADMIN)
      .assertStatusCode(204);
  }

  @Test
  public void nonAdminUserShouldChangeOwnPassword() {
    String newPassword = "pass1";
    String username = "user1";
    String password = "pass";
    TestData.createUser(username, password,false,"xml", "em@l.de");
    // user change the own password
    ScmRequests.start()
      .requestIndexResource(username, password)
      .requestMe()
      .assertStatusCode(200)
      .requestChangePassword(password, newPassword)
      .assertStatusCode(204);
    // assert password is changed -> login with the new Password than undo changes
    ScmRequests.start()
      .requestIndexResource(username, newPassword)
      .requestMe()
      .assertStatusCode(200);

  }

  @Test
  public void shouldHidePasswordLinkIfUserTypeIsNotXML() {
    String newUser = "user";
    String password = "pass";
    String type = "not XML Type";
    TestData.createUser(newUser, password, true, type, "user@scm-manager.org");
    ScmRequests.start()
      .requestIndexResource(newUser, password)
      .requestMe()
      .assertStatusCode(200)
      .assertPasswordLinkDoesNotExists();
  }
}

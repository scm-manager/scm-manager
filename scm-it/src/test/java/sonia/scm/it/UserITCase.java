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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.TestData;

import static org.assertj.core.api.Assertions.assertThat;

public class UserITCase {

  @Before
  public void init(){
    TestData.cleanup();
  }

  @Test
  public void adminShouldChangeOwnPassword() {
    String newUser = "user";
    String password = "pass";
    TestData.createUser(newUser, password, true, "xml", "user@scm-manager.org");
    String newPassword = "new_password";
    // admin change the own password
    ScmRequests.start()
      .requestIndexResource(newUser, password)
      .assertStatusCode(200)
      .requestUser(newUser)
      .assertStatusCode(200)
      .assertPassword(Assert::assertNull)
      .requestChangePassword(newPassword)
      .assertStatusCode(204);
    // assert password is changed -> login with the new Password
    ScmRequests.start()
      .requestIndexResource(newUser, newPassword)
      .assertStatusCode(200)
      .requestUser(newUser)
      .assertPassword(Assert::assertNull);
  }

  @Test
  public void adminShouldChangePasswordOfOtherUser() {
    String newUser = "user";
    String password = "pass";
    TestData.createUser(newUser, password, true, "xml", "user@scm-manager.org");
    String newPassword = "new_password";
    // admin change the password of the user
    ScmRequests.start()
      .requestIndexResource(TestData.USER_SCM_ADMIN, TestData.USER_SCM_ADMIN)
      .assertStatusCode(200)
      .requestUser(newUser)
      .assertStatusCode(200)
      .assertPassword(Assert::assertNull)
      .requestChangePassword(newPassword) // the oldPassword is not needed in the user resource
      .assertStatusCode(204);
    // assert password is changed
    ScmRequests.start()
      .requestIndexResource(newUser, newPassword)
      .assertStatusCode(200)
      .requestUser(newUser)
      .assertStatusCode(200);

  }

  @Test
  public void nonAdminUserShouldNotChangePasswordOfOtherUser() {
    String user = "user";
    String password = "pass";
    TestData.createUser(user, password, false, "xml", "em@l.de");
    String user2 = "user2";
    TestData.createUser(user2, password, false, "xml", "em@l.de");
    ScmRequests.start()
      .requestIndexResource(user, password)
      .assertUsersLinkDoesNotExists();
    // use the users/ endpoint bypassed the index resource
    ScmRequests.start()
      .requestUser(user, password, user2)
      .assertStatusCode(403);
    // use the users/password endpoint bypassed the index and users resources
    ScmRequests.start()
      .requestUserChangePassword(user, password, user2, "newPassword")
      .assertStatusCode(403);
  }

  @Test
  public void shouldHidePasswordLinkIfUserTypeIsNotXML() {
    String newUser = "user";
    String password = "pass";
    String type = "not XML Type";
    TestData.createUser(newUser, password, true, type, "user@scm-manager.org");
    ScmRequests.start()
      .requestIndexResource(newUser, password)
      .assertStatusCode(200)
      .requestUser(newUser)
      .assertStatusCode(200)
      .assertPassword(Assert::assertNull)
      .assertType(s -> assertThat(s).isEqualTo(type))
      .assertPasswordLinkDoesNotExists();
  }
}

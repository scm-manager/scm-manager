/**
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

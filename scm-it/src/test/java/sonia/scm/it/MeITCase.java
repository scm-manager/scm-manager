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

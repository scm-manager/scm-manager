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
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.TRUE))
      .assertPassword(Assert::assertNull)
      .requestChangePassword(password, newPassword) // the oldPassword is needed when the own password should be changed
      .assertStatusCode(204);
    // assert password is changed -> login with the new Password
    ScmRequests.start()
      .requestIndexResource(newUser, newPassword)
      .assertStatusCode(200)
      .requestUser(newUser)
      .assertAdmin(isAdmin -> assertThat(isAdmin).isEqualTo(Boolean.TRUE))
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
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.TRUE)) // the user anonymous is not an admin
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
  public void nonAdminUserShouldChangeOwnPassword() {
    String newUser = "user1";
    String password = "pass";
    TestData.createUser(newUser, password, false, "xml", "em@l.de");
    String newPassword = "new_password";
    ScmRequests.start()
      .requestIndexResource(newUser, password)
      .assertUsersLinkDoesNotExists();
    // use the users/password endpoint bypassed the index resource
    ScmRequests.start()
      .requestUser(newUser, password, newUser)
      .assertStatusCode(200)
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.FALSE))
      .requestChangePassword(password, newPassword) // the oldPassword is needed when the own password should be changed
      .assertStatusCode(204);
//    // assert password is changed -> login with the new Password
    ScmRequests.start()
      .requestUser(newUser, newPassword, newUser)
      .assertStatusCode(200)
      .assertAdmin(isAdmin -> assertThat(isAdmin).isEqualTo(Boolean.FALSE))
      .assertPassword(Assert::assertNull);
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
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.TRUE))
      .assertPassword(Assert::assertNull)
      .assertType(s -> assertThat(s).isEqualTo(type))
      .assertPasswordLinkDoesNotExists();
  }
}

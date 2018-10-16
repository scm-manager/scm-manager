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
    TestData.createUser(newUser, password, true, "xml");
    String newPassword = "new_password";
    // admin change the own password
    ScmRequests.start()
      .given()
      .url(TestData.getUserUrl(newUser))
      .usernameAndPassword(newUser, password)
      .getUserResource()
      .assertStatusCode(200)
      .usingUserResponse()
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.TRUE))
      .assertPassword(Assert::assertNull)
      .requestChangePassword(password, newPassword) // the oldPassword is needed when the own password should be changed
      .assertStatusCode(204);
    // assert password is changed -> login with the new Password
    ScmRequests.start()
      .given()
      .url(TestData.getUserUrl(newUser))
      .usernameAndPassword(newUser, newPassword)
      .getUserResource()
      .assertStatusCode(200)
      .usingUserResponse()
      .assertAdmin(isAdmin -> assertThat(isAdmin).isEqualTo(Boolean.TRUE))
      .assertPassword(Assert::assertNull);

  }

  @Test
  public void adminShouldChangePasswordOfOtherUser() {
    String newUser = "user";
    String password = "pass";
    TestData.createUser(newUser, password, true, "xml");
    String newPassword = "new_password";
    // admin change the password of the user
    ScmRequests.start()
      .given()
      .url(TestData.getUserUrl(newUser))// the admin get the user object
      .usernameAndPassword(TestData.USER_SCM_ADMIN, TestData.USER_SCM_ADMIN)
      .getUserResource()
      .assertStatusCode(200)
      .usingUserResponse()
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.TRUE))
      .assertPassword(Assert::assertNull)
      .requestChangePassword(newPassword) // the oldPassword is not needed in the user resource
      .assertStatusCode(204);
    // assert password is changed
    ScmRequests.start()
      .given()
      .url(TestData.getUserUrl(newUser))
      .usernameAndPassword(newUser, newPassword)
      .getUserResource()
      .assertStatusCode(200);

  }

  @Test
  public void nonAdminUserShouldNotChangePasswordOfOtherUser() {
    String user = "user";
    String password = "pass";
    TestData.createUser(user, password, false, "xml");
    String user2 = "user2";
    TestData.createUser(user2, password, false, "xml");
    ScmRequests.start()
      .given()
      .url(TestData.getUserUrl(user2))
      .usernameAndPassword(user, password)
      .getUserResource()
      .assertStatusCode(403);
  }

    @Test
  public void nonAdminUserShouldChangeOwnPassword() {
    String newUser = "user";
    String password = "pass";
    TestData.createUser(newUser, password, false, "xml");
    String newPassword = "new_password";
    ScmRequests.start()
      .given()
      .url(TestData.getUserUrl(newUser))
      .usernameAndPassword(newUser, password)
      .getUserResource()
      .assertStatusCode(200)
      .usingUserResponse()
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.FALSE))
      .requestChangePassword(password, newPassword) // the oldPassword is needed when the own password should be changed
      .assertStatusCode(204);
    // assert password is changed -> login with the new Password
    ScmRequests.start()
      .given()
      .url(TestData.getUserUrl(newUser))
      .usernameAndPassword(newUser, newPassword)
      .getUserResource()
      .assertStatusCode(200)
      .usingUserResponse()
      .assertAdmin(isAdmin -> assertThat(isAdmin).isEqualTo(Boolean.FALSE))
      .assertPassword(Assert::assertNull);
  }

  @Test
  public void shouldHidePasswordLinkIfUserTypeIsNotXML() {
    String newUser = "user";
    String password = "pass";
    String type = "not XML Type";
    TestData.createUser(newUser, password, true, type);
    ScmRequests.start()
      .given()
      .url(TestData.getMeUrl())
      .usernameAndPassword(newUser, password)
      .getUserResource()
      .assertStatusCode(200)
      .usingUserResponse()
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.TRUE))
      .assertPassword(Assert::assertNull)
      .assertType(s -> assertThat(s).isEqualTo(type))
      .assertPasswordLinkDoesNotExists();
  }
}

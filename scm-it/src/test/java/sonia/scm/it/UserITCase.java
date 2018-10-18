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
      .requestChangePassword(newPassword) // the oldPassword is not needed in the user resource
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

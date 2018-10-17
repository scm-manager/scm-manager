package sonia.scm.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.TestData;

import static org.assertj.core.api.Assertions.assertThat;

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
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.TRUE))
      .assertPassword(Assert::assertNull)
      .assertType(s -> assertThat(s).isEqualTo("xml"))
      .requestChangePassword(TestData.USER_SCM_ADMIN, newPassword)
      .assertStatusCode(204);
    // assert password is changed -> login with the new Password than undo changes
    ScmRequests.start()
      .requestIndexResource(TestData.USER_SCM_ADMIN, newPassword)
      .requestMe()
      .assertStatusCode(200)
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.TRUE))// still admin
      .requestChangePassword(newPassword, TestData.USER_SCM_ADMIN)
      .assertStatusCode(204);
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
      .assertAdmin(aBoolean -> assertThat(aBoolean).isEqualTo(Boolean.TRUE))
      .assertPassword(Assert::assertNull)
      .assertType(s -> assertThat(s).isEqualTo(type))
      .assertPasswordLinkDoesNotExists();
  }
}

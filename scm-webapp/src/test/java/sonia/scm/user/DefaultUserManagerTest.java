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

package sonia.scm.user;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import sonia.scm.NotFoundException;
import sonia.scm.store.JAXBConfigurationStoreFactory;
import sonia.scm.user.xml.XmlUserDAO;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Sdorra
 */
@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class DefaultUserManagerTest extends UserManagerTestBase {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private UserDAO userDAO;

  /**
   * Method description
   *
   * @return
   */
  @Override
  public UserManager createManager() {
    return new DefaultUserManager(createXmlUserDAO());
  }

  @Before
  public void initDao() {
    User trillian = UserTestData.createTrillian();
    trillian.setPassword("oldEncrypted");

    userDAO = mock(UserDAO.class);
    when(userDAO.getType()).thenReturn("xml");
    when(userDAO.get("trillian")).thenReturn(trillian);
  }

  @Test(expected = InvalidPasswordException.class)
  public void shouldFailChangePasswordForWrongOldPassword() {
    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.changePasswordForLoggedInUser("wrongPassword", "$shiro1$secret");
  }

  @Test
  public void shouldSucceedChangePassword() {
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    doNothing().when(userDAO).modify(userCaptor.capture());

    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.changePasswordForLoggedInUser("oldEncrypted", "newEncrypted");

    Assertions.assertThat(userCaptor.getValue().getPassword()).isEqualTo("newEncrypted");
  }

  @Test(expected = NotFoundException.class)
  public void shouldFailOverwritePasswordForMissingUser() {
    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.overwritePassword("notExisting", "---");
  }

  @Test(expected = ChangePasswordNotAllowedException.class)
  public void shouldFailOverwritePasswordForExternalUser() {
    User trillian = new User("trillian");
    trillian.setExternal(true);
    when(userDAO.get("trillian")).thenReturn(trillian);
    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.overwritePassword("trillian", "---");
  }

  @Test
  public void shouldSucceedOverwritePassword() {
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    doNothing().when(userDAO).modify(userCaptor.capture());

    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.overwritePassword("trillian", "newEncrypted");

    Assertions.assertThat(userCaptor.getValue().getPassword()).isEqualTo("newEncrypted");
  }

  //~--- methods --------------------------------------------------------------

  private XmlUserDAO createXmlUserDAO() {
    return new XmlUserDAO(new JAXBConfigurationStoreFactory(contextProvider, locationResolver, null));
  }
}

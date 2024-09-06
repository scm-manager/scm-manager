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

package sonia.scm.user;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.authc.credential.PasswordService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import sonia.scm.NotFoundException;
import sonia.scm.store.JAXBConfigurationStoreFactory;
import sonia.scm.store.StoreCacheConfigProvider;
import sonia.scm.user.xml.XmlUserDAO;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class DefaultUserManagerTest extends UserManagerTestBase {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final UserDAO userDAO = mock(UserDAO.class);
  private final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
  private final PasswordService passwordService = mock(PasswordService.class);

  private UserManager userManager;

  @Override
  public UserManager createManager() {
    return new DefaultUserManager(passwordService, createXmlUserDAO(), emptySet());
  }

  @Before
  public void initMocks() {
    User trillian = UserTestData.createTrillian();
    trillian.setPassword("oldEncrypted");

    when(userDAO.getType()).thenReturn("xml");
    when(userDAO.get("trillian")).thenReturn(trillian);
    doNothing().when(userDAO).modify(userCaptor.capture());

    when(passwordService.encryptPassword(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

    userManager = new DefaultUserManager(passwordService, userDAO, emptySet());
  }

  @Test(expected = InvalidPasswordException.class)
  public void shouldFailChangePasswordForWrongOldPassword() {
    userManager.changePasswordForLoggedInUser("wrongPassword", "$shiro1$secret");
  }

  @Test
  public void shouldSucceedChangePassword() {
    userManager.changePasswordForLoggedInUser("oldEncrypted", "newEncrypted");

    Assertions.assertThat(userCaptor.getValue().getPassword()).isEqualTo("newEncrypted");
  }

  @Test
  public void shouldEncryptChangedPassword() {
    when(passwordService.encryptPassword("newPassword")).thenReturn("newEncrypted");

    userManager.changePasswordForLoggedInUser("oldEncrypted", "newPassword");

    Assertions.assertThat(userCaptor.getValue().getPassword()).isEqualTo("newEncrypted");
  }

  @Test(expected = NotFoundException.class)
  public void shouldFailOverwritePasswordForMissingUser() {
    userManager.overwritePassword("notExisting", "---");
  }

  @Test(expected = ChangePasswordNotAllowedException.class)
  public void shouldFailOverwritePasswordForExternalUser() {
    User trillian = new User("trillian");
    trillian.setExternal(true);
    when(userDAO.get("trillian")).thenReturn(trillian);

    userManager.overwritePassword("trillian", "---");
  }

  @Test
  public void shouldSucceedOverwritePassword() {
    userManager.overwritePassword("trillian", "newEncrypted");

    Assertions.assertThat(userCaptor.getValue().getPassword()).isEqualTo("newEncrypted");
  }

  @Test
  public void shouldEncryptOverwrittenPassword() {
    when(passwordService.encryptPassword("newPassword")).thenReturn("newEncrypted");

    userManager.overwritePassword("trillian", "newPassword");

    Assertions.assertThat(userCaptor.getValue().getPassword()).isEqualTo("newEncrypted");
  }

  @Test
  public void shouldEncryptPasswordOnModify() {
    User zaphod = UserTestData.createZaphod();
    when(passwordService.encryptPassword("password")).thenReturn("encrypted");

    manager.create(zaphod);
    zaphod.setPassword("password");
    manager.modify(zaphod);

    User otherUser = manager.get("zaphod");

    assertNotNull(otherUser);
    assertEquals("encrypted" , otherUser.getPassword());
  }

  @Test
  public void shouldEncryptPasswordOnCreate() {
    User zaphod = UserTestData.createZaphod();
    zaphod.setPassword("password");
    when(passwordService.encryptPassword("password")).thenReturn("encrypted");

    manager.create(zaphod);

    User otherUser = manager.get("zaphod");

    assertEquals("encrypted", otherUser.getPassword());
  }

  private XmlUserDAO createXmlUserDAO() {
    return new XmlUserDAO(new JAXBConfigurationStoreFactory(contextProvider, locationResolver, null, emptySet(), new StoreCacheConfigProvider(false)));
  }
}

/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.Lists;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import sonia.scm.NotFoundException;
import sonia.scm.store.JAXBConfigurationStoreFactory;
import sonia.scm.user.xml.XmlUserDAO;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import java.util.List;
import org.junit.Rule;

/**
 *
 * @author Sebastian Sdorra
 */
@SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class DefaultUserManagerTest extends UserManagerTestBase
{

  @Rule
  public ShiroRule shiro = new ShiroRule();


  private UserDAO userDAO ;
  private User trillian;

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public UserManager createManager()
  {
    return new DefaultUserManager(createXmlUserDAO());
  }

  @Before
  public void initDao() {
    trillian = UserTestData.createTrillian();
    trillian.setPassword("oldEncrypted");

    userDAO = mock(UserDAO.class);
    when(userDAO.getType()).thenReturn("xml");
    when(userDAO.get("trillian")).thenReturn(trillian);
  }

  @Test(expected = InvalidPasswordException.class)
  public void shouldFailChangePasswordForWrongOldPassword() {
    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.changePasswordForLoggedInUser("wrongPassword", "---");
  }

  @Test
  public void shouldSucceedChangePassword() {
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    doNothing().when(userDAO).modify(userCaptor.capture());

    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.changePasswordForLoggedInUser("oldEncrypted", "newEncrypted");

    Assertions.assertThat(userCaptor.getValue().getPassword()).isEqualTo("newEncrypted");
  }

  @Test(expected = ChangePasswordNotAllowedException.class)
  public void shouldFailOverwritePasswordForWrongType() {
    trillian.setType("wrongType");

    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.overwritePassword("trillian", "---");
  }

  @Test(expected = NotFoundException.class)
  public void shouldFailOverwritePasswordForMissingUser() {
    UserManager userManager = new DefaultUserManager(userDAO);

    userManager.overwritePassword("notExisting", "---");
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
    return new XmlUserDAO(new JAXBConfigurationStoreFactory(contextProvider, locationResolver));
  }
}

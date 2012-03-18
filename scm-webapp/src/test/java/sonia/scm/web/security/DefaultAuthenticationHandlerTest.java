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



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;

import org.junit.Test;

import sonia.scm.AbstractTestBase;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.security.MessageDigestEncryptionHandler;
import sonia.scm.store.JAXBStoreFactory;
import sonia.scm.store.StoreFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserListener;
import sonia.scm.user.UserTestData;
import sonia.scm.user.DefaultUserManager;
import sonia.scm.util.MockUtil;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sonia.scm.user.xml.XmlUserDAO;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultAuthenticationHandlerTest extends AbstractTestBase
{

  /**
   * Method description
   *
   */
  @Test
  public void testAuthenticateFailed()
  {
    AuthenticationResult result = handler.authenticate(request, reponse,
                                    slarti.getName(), "otherPWD");

    assertNotNull(result);
    assertTrue(result.getState() == AuthenticationState.FAILED);
    assertNull(result.getUser());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAuthenticateNotFound()
  {
    AuthenticationResult result = handler.authenticate(request, reponse,
                                    "notSlarti", "otherPWD");

    assertNotNull(result);
    assertTrue(result.getState() == AuthenticationState.NOT_FOUND);
    assertNull(result.getUser());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAuthenticateSuccess()
  {
    AuthenticationResult result = handler.authenticate(request, reponse,
                                    slarti.getName(), "slartisPWD");

    assertNotNull(result);
    assertTrue(result.getState() == AuthenticationState.SUCCESS);
    assertNotNull(result.getUser());
    assertEquals(slarti.getName(), result.getUser().getName());
    assertEquals(slarti.getDisplayName(), result.getUser().getDisplayName());
    assertEquals(slarti.getMail(), result.getUser().getMail());
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  protected void postSetUp() throws Exception
  {
    EncryptionHandler enc = new MessageDigestEncryptionHandler();

    slarti = UserTestData.createSlarti();
    slarti.setPassword(enc.encrypt("slartisPWD"));

    StoreFactory storeFactory = new JAXBStoreFactory();

    storeFactory.init(contextProvider);

    Provider<Set<UserListener>> listenerProvider = mock(Provider.class);

    when(listenerProvider.get()).thenReturn(new HashSet<UserListener>());

    XmlUserDAO userDAO = new XmlUserDAO(storeFactory);
    
    DefaultUserManager userManager =
      new DefaultUserManager(MockUtil.getAdminSecurityContextProvider(),
                         userDAO, listenerProvider);

    userManager.init(contextProvider);
    userManager.create(slarti);
    handler = new DefaultAuthenticationHandler(userManager, enc);
    handler.init(contextProvider);
    request = MockUtil.getHttpServletRequest();
    reponse = MockUtil.getHttpServletResponse();
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  protected void preTearDown() throws Exception
  {
    handler.close();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private DefaultAuthenticationHandler handler;

  /** Field description */
  private HttpServletResponse reponse;

  /** Field description */
  private HttpServletRequest request;

  /** Field description */
  private User slarti;
}

/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;

import org.apache.http.annotation.Immutable;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;

import org.junit.Test;

import sonia.scm.cache.MapCacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserTestData;
import sonia.scm.web.security.AuthenticationManager;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmRealmTest
{

  /**
   * Method description
   *
   */
  @Test(expected = UnknownAccountException.class)
  public void testAuthenticationWithUknownUser()
  {
    User trillian = createSampleUser();
    ScmRealm realm = createRealm(trillian);

    realm.getAuthenticationInfo(token("marvin", trillian.getPassword()));
  }

  /**
   * Method description
   *
   */
  @Test(expected = AccountException.class)
  public void testFailedAuthentication()
  {
    User trillian = createSampleUser();
    ScmRealm realm = createRealm(trillian);

    realm.getAuthenticationInfo(token(trillian.getId(), "hobbo"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSimpleAuthentication()
  {
    User trillian = createSampleUser();

    ScmRealm realm = createRealm(trillian, ImmutableSet.of("g1", "g2"));
    AuthenticationInfo ai = realm.getAuthenticationInfo(token(trillian));

    assertNotNull(ai);

    PrincipalCollection collection = ai.getPrincipals();

    assertNotNull(collection);
    assertFalse(collection.isEmpty());

    assertEquals(trillian.getId(), collection.getPrimaryPrincipal());
    assertEquals(trillian, collection.oneByType(User.class));

    GroupNames groups = collection.oneByType(GroupNames.class);

    assertNotNull(groups);
    assertFalse(groups.getCollection().isEmpty());
    assertThat(groups, containsInAnyOrder("g1", "g2"));
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   */
  private ScmRealm createRealm(User user)
  {
    return createRealm(user, null);
  }

  /**
   * Method description
   *
   *
   *
   * @param user
   * @param groups
   * @return
   */
  private ScmRealm createRealm(User user, Collection<String> groups)
  {
    UserManager userManager = mock(UserManager.class);
    GroupManager groupManager = mock(GroupManager.class);
    RepositoryManager repositoryManager = mock(RepositoryManager.class);
    RepositoryDAO repositoryDAO = mock(RepositoryDAO.class);
    UserDAO userDAO = mock(UserDAO.class);

    when(userDAO.get(user.getId())).thenReturn(user);

    HttpSession session = mock(HttpSession.class);

    final HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getSession(true)).thenReturn(session);

    Provider<HttpServletRequest> requestProvider =
      new Provider<HttpServletRequest>()
    {

      @Override
      public HttpServletRequest get()
      {
        return request;
      }
    };

    final HttpServletResponse response = mock(HttpServletResponse.class);
    Provider<HttpServletResponse> responseProvider =
      new Provider<HttpServletResponse>()
    {

      @Override
      public HttpServletResponse get()
      {
        return response;
      }
    };

    //J-
    AuthenticationManager authManager = mock(AuthenticationManager.class);
    
    when( 
      authManager.authenticate(
        eq(requestProvider.get()),
        eq(responseProvider.get()),
        eq(user.getId()),
        eq(user.getPassword())
      ) 
    ).thenReturn( 
      new AuthenticationResult(user, groups, AuthenticationState.SUCCESS) 
    );
    
    when( 
      authManager.authenticate(
        eq(requestProvider.get()),
        eq(responseProvider.get()),
        eq(user.getId()),
        argThat(
          not(user.getPassword()) 
        )
      ) 
    ).thenReturn( 
      AuthenticationResult.FAILED
    );

    when( 
      authManager.authenticate(
        eq(requestProvider.get()),
        eq(responseProvider.get()),
        argThat(
          not(user.getName())
        ),
        anyString()
      ) 
    ).thenReturn( 
      AuthenticationResult.NOT_FOUND
    );
    

    return new ScmRealm(
      new ScmConfiguration(),
      new MapCacheManager(),
      userManager,
      groupManager,
      repositoryManager,
      repositoryDAO,
      userDAO,
      authManager,
      requestProvider,
      responseProvider
    );
    //J+
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private User createSampleUser()
  {
    User trillian = UserTestData.createTrillian();

    trillian.setPassword("moppo123");

    return trillian;
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   */
  private AuthenticationToken token(User user)
  {
    return new UsernamePasswordToken(user.getId(), user.getPassword());
  }

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   *
   * @return
   */
  private AuthenticationToken token(String username, String password)
  {
    return new UsernamePasswordToken(username, password);
  }
}

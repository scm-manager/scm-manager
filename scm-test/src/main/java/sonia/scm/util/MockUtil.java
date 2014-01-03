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



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.Subject.Builder;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import sonia.scm.SCMContextProvider;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sonia.scm.security.Role;

/**
 *
 * @author Sebastian Sdorra
 */
public final class MockUtil
{

  /** Field description */
  private static final User ADMIN = new User("scmadmin", "SCM Admin",
                                      "scmadmin@scm.org");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private MockUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static Subject createAdminSubject()
  {
    Subject subject = mock(Subject.class);

    when(subject.isAuthenticated()).thenReturn(Boolean.TRUE);
    when(subject.isPermitted(anyListOf(Permission.class))).then(
      new Answer<Boolean[]>()
    {

      @Override
      public Boolean[] answer(InvocationOnMock invocation) throws Throwable
      {
        List<Permission> permissions =
          (List<Permission>) invocation.getArguments()[0];
        Boolean[] returnArray = new Boolean[permissions.size()];

        Arrays.fill(returnArray, Boolean.TRUE);

        return returnArray;
      }
    });
    when(subject.isPermitted(any(Permission.class))).thenReturn(Boolean.TRUE);
    when(subject.isPermitted(any(String.class))).thenReturn(Boolean.TRUE);
    when(subject.isPermittedAll(anyCollectionOf(Permission.class))).thenReturn(
      Boolean.TRUE);
    when(subject.isPermittedAll()).thenReturn(Boolean.TRUE);
    when(subject.hasRole(Role.ADMIN)).thenReturn(Boolean.TRUE);
    when(subject.hasRole(Role.USER)).thenReturn(Boolean.TRUE);

    PrincipalCollection collection = mock(PrincipalCollection.class);

    when(collection.getPrimaryPrincipal()).thenReturn(ADMIN.getId());
    when(collection.oneByType(User.class)).thenReturn(ADMIN);

    when(subject.getPrincipal()).thenReturn(ADMIN.getId());
    when(subject.getPrincipals()).thenReturn(collection);

    return subject;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static Subject createUserSubject()
  {
    return createUserSubject(null);
  }

  /**
   * Method description
   *
   *
   *
   * @param securityManager
   * @return
   */
  public static Subject createUserSubject(
    org.apache.shiro.mgt.SecurityManager securityManager)
  {
    SimplePrincipalCollection collection = new SimplePrincipalCollection();
    User user = UserTestData.createTrillian();

    collection.add(user.getName(), "junit");
    collection.add(user, "junit");

    Builder builder;

    if (securityManager != null)
    {
      builder = new Subject.Builder(securityManager);
    }
    else
    {
      builder = new Subject.Builder();
    }

    return builder.principals(collection).authenticated(true).buildSubject();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static HttpServletRequest getHttpServletRequest()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getContextPath()).thenReturn("/scm-webapp");

    return request;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static HttpServletResponse getHttpServletResponse()
  {
    return mock(HttpServletResponse.class);
  }

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   */
  public static SCMContextProvider getSCMContextProvider(File directory)
  {
    SCMContextProvider provider = mock(SCMContextProvider.class);

    when(provider.getBaseDirectory()).thenReturn(directory);

    return provider;
  }
}

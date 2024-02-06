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
    
package sonia.scm.util;



import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.Subject.Builder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import sonia.scm.SCMContextProvider;
import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public final class MockUtil
{

  private static final User ADMIN = new User("scmadmin", "SCM Admin",
                                      "scmadmin@scm.org");


  private MockUtil() {}


  
  @SuppressWarnings("unchecked")
  public static Subject createAdminSubject()
  {
    Subject subject = mock(Subject.class);

    when(subject.isAuthenticated()).thenReturn(Boolean.TRUE);
    when(subject.isPermitted(anyList())).then(
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
    when(subject.isPermittedAll(anyCollection())).thenReturn(
      Boolean.TRUE);
    when(subject.isPermittedAll()).thenReturn(Boolean.TRUE);
    when(subject.hasRole(Role.USER)).thenReturn(Boolean.TRUE);

    PrincipalCollection collection = mock(PrincipalCollection.class);

    when(collection.getPrimaryPrincipal()).thenReturn(ADMIN.getId());
    when(collection.oneByType(User.class)).thenReturn(ADMIN);

    when(subject.getPrincipal()).thenReturn(ADMIN.getId());
    when(subject.getPrincipals()).thenReturn(collection);

    return subject;
  }

  
  public static Subject createUserSubject()
  {
    return createUserSubject(null);
  }

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


  
  public static HttpServletRequest getHttpServletRequest()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getContextPath()).thenReturn("/scm-webapp");

    return request;
  }

  
  public static HttpServletResponse getHttpServletResponse()
  {
    return mock(HttpServletResponse.class);
  }


  public static SCMContextProvider getSCMContextProvider(File directory)
  {
    SCMContextProvider provider = mock(SCMContextProvider.class);

    lenient().when(provider.getBaseDirectory()).thenReturn(directory);
    lenient().when(provider.resolve(any(Path.class))).then(ic -> {
      Path p = ic.getArgument(0);
      return directory.toPath().resolve(p);
    });

    return provider;
  }
}

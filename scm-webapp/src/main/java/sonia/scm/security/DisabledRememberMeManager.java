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
    
package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;

/**
 * Remember me manager implementation which does nothing. The
 * DisabledRememberMeManager is used to disable the cookie creation of the
 * default {@link RememberMeManager}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public class DisabledRememberMeManager implements RememberMeManager
{

  /**
   * The implementation does nothing.
   *
   *
   * @param subjectContext subject context
   */
  @Override
  public void forgetIdentity(SubjectContext subjectContext)
  {

    // do nothing
  }

  /**
   * The implementation does nothing.
   *
   *
   * @param subject subject
   * @param token authentication token
   * @param ae authentication exception
   */
  @Override
  public void onFailedLogin(Subject subject, AuthenticationToken token,
    AuthenticationException ae)
  {

    // do nothing
  }

  /**
   * The implementation does nothing.
   *
   *
   * @param subject subject
   */
  @Override
  public void onLogout(Subject subject)
  {

    // do nothing
  }

  /**
   * The implementation does nothing.
   *
   *
   * @param subject subject
   * @param token authentication token
   * @param info authentication info
   */
  @Override
  public void onSuccessfulLogin(Subject subject, AuthenticationToken token,
    AuthenticationInfo info)
  {

    // do nothing
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * The implementation returns always {@code null}.
   *
   *
   * @param subjectContext subject context
   *
   * @return always {@code null}
   */
  @Override
  public PrincipalCollection getRememberedPrincipals(
    SubjectContext subjectContext)
  {
    return null;
  }
}

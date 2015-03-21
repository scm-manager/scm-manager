/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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
    throw new UnsupportedOperationException("Not supported yet.");    // To change body of generated methods, choose Tools | Templates.
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

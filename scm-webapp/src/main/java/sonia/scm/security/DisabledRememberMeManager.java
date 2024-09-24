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

package sonia.scm.security;


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
 * @since 2.0.0
 */
public class DisabledRememberMeManager implements RememberMeManager
{

  /**
   * The implementation does nothing.
   */
  @Override
  public void forgetIdentity(SubjectContext subjectContext)
  {

    // do nothing
  }

  /**
   * The implementation does nothing.
   */
  @Override
  public void onFailedLogin(Subject subject, AuthenticationToken token,
    AuthenticationException ae)
  {

    // do nothing
  }

  /**
   * The implementation does nothing.
   */
  @Override
  public void onLogout(Subject subject)
  {

    // do nothing
  }

  /**
   * The implementation does nothing.
   */
  @Override
  public void onSuccessfulLogin(Subject subject, AuthenticationToken token,
    AuthenticationInfo info)
  {

    // do nothing
  }


  /**
   * The implementation returns always {@code null}.
   */
  @Override
  public PrincipalCollection getRememberedPrincipals(
    SubjectContext subjectContext)
  {
    return null;
  }
}

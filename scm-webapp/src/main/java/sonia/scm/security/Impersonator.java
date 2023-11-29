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

import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Impersonator allows the usage of scm-manager api in the context of another user.
 *
 * @since 2.23.0
 */
public final class Impersonator {

  private static final Logger LOG = LoggerFactory.getLogger(Impersonator.class);

  private final SecurityManager securityManager;

  @Inject
  public Impersonator(SecurityManager securityManager) {
    this.securityManager = securityManager;
  }

  public Session impersonate(PrincipalCollection principal) {
    Subject subject = createSubject(principal);
    if (ThreadContext.getSecurityManager() != null) {
      return new WebImpersonator(subject);
    }
    return new NonWebImpersonator(securityManager, subject);
  }

  private Subject createSubject(PrincipalCollection principal) {
    return new Subject.Builder(securityManager)
      .authenticated(true)
      .principals(principal)
      .buildSubject();
  }

  public interface Session extends AutoCloseable {
    void close();
  }

  private static class WebImpersonator implements Session {

    private final Subject subject;
    private final Subject previousSubject;

    private WebImpersonator(Subject subject) {
      this.subject = subject;
      this.previousSubject = SecurityUtils.getSubject();
      bind();
    }

    private void bind() {
      LOG.debug("user {} start impersonate session as {}", previousSubject.getPrincipal(), subject.getPrincipal());


      // do not use runas, because we want only bind the session to this thread.
      // Runas could affect other threads.
      ThreadContext.bind(this.subject);
    }

    @Override
    public void close() {
      LOG.debug("release impersonate session from user {} to {}", previousSubject.getPrincipal(), subject.getPrincipal());
      ThreadContext.bind(previousSubject);
    }

  }

  private static class NonWebImpersonator implements Session {

    private final SecurityManager securityManager;
    private final SubjectThreadState state;
    private final Subject subject;

    private NonWebImpersonator(SecurityManager securityManager, Subject subject) {
      this.securityManager = securityManager;
      this.state = new SubjectThreadState(subject);
      this.subject = subject;
      bind();
    }

    private void bind() {
      LOG.debug("start impersonate session as user {}", subject.getPrincipal());
      SecurityUtils.setSecurityManager(securityManager);
      state.bind();
    }

    @Override
    public void close() {
      LOG.debug("release impersonate session of {}", subject.getPrincipal());
      state.restore();
      SecurityUtils.setSecurityManager(null);
    }
  }

}

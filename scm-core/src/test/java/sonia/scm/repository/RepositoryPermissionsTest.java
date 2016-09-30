/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.repository;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import java.util.function.Supplier;
import org.apache.shiro.authz.UnauthorizedException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.security.PermissionActionCheck;
import sonia.scm.security.PermissionCheck;

/**
 * Unit tests for {@link RepositoryPermissions}.
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/repository/repository-permissions.ini")
public class RepositoryPermissionsTest {

  @Mock
  private Repository repository;

  /**
   * Shiro rule.
   */
  @Rule
  public ShiroRule shiro = new ShiroRule();

  /**
   * Test permission checks with id successful.
   */
  @Test
  @SubjectAware(username = "permitted", password = "secret")
  public void testPermissionCheckWithId() {
    assertPermissionCheckId(RepositoryPermissions::read);
    assertPermissionCheckId(RepositoryPermissions::write);
    assertPermissionCheckId(RepositoryPermissions::delete);
    assertPermissionCheckId(RepositoryPermissions::modify);
    assertPermissionCheckId(RepositoryPermissions::healthCheck);
  }

  /**
   * Test permission checks with id successful.
   */
  @Test
  @SubjectAware(username = "permitted", password = "secret")
  public void testPermissionActionCheck() {
    assertPermissionActionCheck(RepositoryPermissions::read);
    assertPermissionActionCheck(RepositoryPermissions::write);
    assertPermissionActionCheck(RepositoryPermissions::delete);
    assertPermissionActionCheck(RepositoryPermissions::modify);
    assertPermissionActionCheck(RepositoryPermissions::healthCheck);
  }

  @Test
  @SubjectAware(username = "notpermitted", password = "secret")
  public void testPermissionActionCheckWithoutRequired() {
    assertFailedPermissionActionCheck(RepositoryPermissions::read);
    assertFailedPermissionActionCheck(RepositoryPermissions::write);
    assertFailedPermissionActionCheck(RepositoryPermissions::delete);
    assertFailedPermissionActionCheck(RepositoryPermissions::modify);
    assertFailedPermissionActionCheck(RepositoryPermissions::healthCheck);
  }
  
  /**
   * Test permission checks with repository successful.
   */
  @Test
  @SubjectAware(username = "permitted", password = "secret")
  public void testPermissionCheckWithRepository() {
    assertPermissionCheckRepository(RepositoryPermissions::read);
    assertPermissionCheckRepository(RepositoryPermissions::write);
    assertPermissionCheckRepository(RepositoryPermissions::delete);
    assertPermissionCheckRepository(RepositoryPermissions::modify);
    assertPermissionCheckRepository(RepositoryPermissions::healthCheck);
  }

  /**
   * Test permission checks with id and without the required permissions.
   */
  @Test
  @SubjectAware(username = "notpermitted", password = "secret")
  public void testPermissionCheckWithIdAndWithoutRequiredPermissions() {
    assertFailedPermissionCheckId(RepositoryPermissions::read);
    assertFailedPermissionCheckId(RepositoryPermissions::write);
    assertFailedPermissionCheckId(RepositoryPermissions::delete);
    assertFailedPermissionCheckId(RepositoryPermissions::modify);
    assertFailedPermissionCheckId(RepositoryPermissions::healthCheck);
  }

  /**
   * Tests permission checks with repository and without the required permissions.
   */
  @Test
  @SubjectAware(username = "notpermitted", password = "secret")
  public void testPermissionCheckWithRepositoryAndWithoutRequiredPermissions() {
    assertFailedPermissionCheckRepository(RepositoryPermissions::read);
    assertFailedPermissionCheckRepository(RepositoryPermissions::write);
    assertFailedPermissionCheckRepository(RepositoryPermissions::delete);
    assertFailedPermissionCheckRepository(RepositoryPermissions::modify);
    assertFailedPermissionCheckRepository(RepositoryPermissions::healthCheck);
  }

  private void assertFailedPermissionCheckRepository(RepositoryPermissionChecker<Repository> checker) {
    when(repository.getId()).thenReturn("abc");
    assertFailedPermissionCheck(checker.get(repository));
  }

  private void assertFailedPermissionCheckId(RepositoryPermissionChecker<String> checker) {
    assertFailedPermissionCheck(checker.get("abc"));
  }

  private void assertFailedPermissionCheck(PermissionCheck check) {
    assertNotNull(check);
    assertFalse(check.isPermitted());
    boolean fail = false;
    try {
      check.check();
    }
    catch (UnauthorizedException ex) {
      fail = true;
    }
    assertTrue(fail);
  }

  private void assertPermissionCheckRepository(RepositoryPermissionChecker<Repository> checker) {
    when(repository.getId()).thenReturn("abc");
    assertPermissionCheck(checker.get(repository));
  }

  private void assertPermissionCheckId(RepositoryPermissionChecker<String> checker) {
    assertPermissionCheck(checker.get("abc"));
  }

  private void assertPermissionCheck(PermissionCheck check) {
    assertNotNull(check);
    assertTrue(check.isPermitted());
    check.check();
  }

  private void assertPermissionActionCheck(Supplier<PermissionActionCheck<Repository>> supplier) {
    assertPermissionActionCheck(supplier.get());
  }

  private void assertFailedPermissionActionCheck(Supplier<PermissionActionCheck<Repository>> supplier) {
    assertFailedPermissionActionCheck(supplier.get());
  }
  
  private void assertFailedPermissionActionCheck(PermissionActionCheck<Repository> check) {
    assertNotNull(check);
    assertFalse(check.isPermitted("abc"));
    boolean fail = false;
    try {
      check.check("abc");
    }
    catch (UnauthorizedException ex) {
      fail = true;
    }
    assertTrue(fail);
    fail = false;
    when(repository.getId()).thenReturn("abc");
    assertFalse(check.isPermitted(repository));
    try {
      check.check(repository);
    }
    catch (UnauthorizedException ex) {
      fail = true;
    }
    assertTrue(fail);
  }

  private void assertPermissionActionCheck(PermissionActionCheck<Repository> check) {
    assertNotNull(check);
    assertTrue(check.isPermitted("abc"));
    check.check("abc");
    when(repository.getId()).thenReturn("abc");
    assertTrue(check.isPermitted(repository));
    check.check(repository);
  }

  @FunctionalInterface
  private static interface RepositoryPermissionChecker<T> {

    public PermissionCheck get(T type);

  }

}

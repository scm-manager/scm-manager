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


package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;

import org.junit.Before;
import org.junit.Test;

import sonia.scm.AbstractTestBase;
import sonia.scm.store.JAXBConfigurationEntryStoreFactory;
import sonia.scm.util.MockUtil;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultSecuritySystemTest extends AbstractTestBase
{

  /**
   * Method description
   *
   */
  @Before
  public void createSecuritySystem()
  {
    JAXBConfigurationEntryStoreFactory factory =
      new JAXBConfigurationEntryStoreFactory(new UUIDKeyGenerator(),
        contextProvider);

    securitySystem = new DefaultSecuritySystem(factory);

    // ScmEventBus.getInstance().register(listener);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAddPermission()
  {
    setAdminSubject();

    StoredAssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    assertEquals("trillian", sap.getName());
    assertEquals("repository:*:READ", sap.getPermission());
    assertEquals(false, sap.isGroupPermission());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAvailablePermissions()
  {
    setAdminSubject();

    List<PermissionDescriptor> list = securitySystem.getAvailablePermissions();

    assertNotNull(list);
    assertThat(list.size(), greaterThan(0));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testDeletePermission()
  {
    setAdminSubject();

    StoredAssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    securitySystem.deletePermission(sap);

    assertNull(securitySystem.getPermission(sap.getId()));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetAllPermissions()
  {
    setAdminSubject();

    StoredAssignedPermission trillian = createPermission("trillian", false,
                                          "repository:*:READ");
    StoredAssignedPermission dent = createPermission("dent", false,
                                      "repository:*:READ");
    StoredAssignedPermission marvin = createPermission("marvin", false,
                                        "repository:*:READ");

    List<StoredAssignedPermission> all = securitySystem.getAllPermissions();

    assertEquals(3, all.size());
    assertThat(all, containsInAnyOrder(trillian, dent, marvin));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetPermission()
  {
    setAdminSubject();

    StoredAssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    StoredAssignedPermission other = securitySystem.getPermission(sap.getId());

    assertEquals(sap.getId(), other.getId());
    assertEquals(sap, other);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetPermissionsWithPredicate()
  {
    setAdminSubject();

    StoredAssignedPermission trillian = createPermission("trillian", false,
                                          "repository:*:READ");
    StoredAssignedPermission dent = createPermission("dent", false,
                                      "repository:*:READ");

    createPermission("hitchhiker", true, "repository:*:READ");

    List<StoredAssignedPermission> filtered =
      securitySystem.getPermissions(new Predicate<AssignedPermission>()
    {

      @Override
      public boolean apply(AssignedPermission input)
      {
        return !input.isGroupPermission();
      }
    });

    assertEquals(2, filtered.size());
    assertThat(filtered, containsInAnyOrder(trillian, dent));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testModifyPermission()
  {
    setAdminSubject();

    StoredAssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");
    StoredAssignedPermission modified =
      new StoredAssignedPermission(sap.getId(),
        new AssignedPermission("trillian", "repository:*:WRITE"));

    securitySystem.modifyPermission(modified);

    sap = securitySystem.getPermission(modified.getId());

    assertEquals(modified.getId(), sap.getId());
    assertEquals(modified, sap);
  }

  /**
   * Method description
   *
   */
  @Test(expected = UnauthorizedException.class)
  public void testUnauthorizedAddPermission()
  {
    setUserSubject();
    createPermission("trillian", false, "repository:*:READ");
  }

  /**
   * Method description
   *
   */
  @Test(expected = UnauthorizedException.class)
  public void testUnauthorizedDeletePermission()
  {
    setAdminSubject();

    StoredAssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    setUserSubject();
    securitySystem.deletePermission(sap);
  }

  /**
   * Method description
   *
   */
  @Test(expected = UnauthorizedException.class)
  public void testUnauthorizedGetPermission()
  {
    setAdminSubject();

    StoredAssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    setUserSubject();
    securitySystem.getPermission(sap.getId());
  }

  /**
   * Method description
   *
   */
  @Test(expected = UnauthorizedException.class)
  public void testUnauthorizedModifyPermission()
  {
    setAdminSubject();

    StoredAssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    setUserSubject();

    securitySystem.modifyPermission(sap);
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param groupPermission
   * @param value
   *
   * @return
   */
  private StoredAssignedPermission createPermission(String name,
    boolean groupPermission, String value)
  {
    AssignedPermission ap = new AssignedPermission(name, groupPermission,
                              value);
    StoredAssignedPermission sap = securitySystem.addPermission(ap);

    assertNotNull(sap);
    assertNotNull(sap.getId());

    return sap;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  private void setAdminSubject()
  {
    setSubject(MockUtil.createAdminSubject());
  }

  /**
   * Method description
   *
   */
  private void setUserSubject()
  {
    org.apache.shiro.mgt.SecurityManager sm =
      new DefaultSecurityManager(new SimpleAccountRealm());

    setSubject(MockUtil.createUserSubject(sm));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private DefaultSecuritySystem securitySystem;
}

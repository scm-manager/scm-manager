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

import com.google.common.base.Objects;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import sonia.scm.AbstractTestBase;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.JAXBConfigurationEntryStoreFactory;
import sonia.scm.util.ClassLoaders;
import sonia.scm.util.MockUtil;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultSecuritySystemTest extends AbstractTestBase
{

  private JAXBConfigurationEntryStoreFactory jaxbConfigurationEntryStoreFactory;
  private PluginLoader pluginLoader;
  @InjectMocks
  private DefaultSecuritySystem securitySystem;


  /**
   * Method description
   *
   */
  @Before
  public void createSecuritySystem()
  {
    jaxbConfigurationEntryStoreFactory =
      spy(new JAXBConfigurationEntryStoreFactory(contextProvider , repositoryLocationResolver, new UUIDKeyGenerator() ) {});
    pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(ClassLoaders.getContextClassLoader(DefaultSecuritySystem.class));

    MockitoAnnotations.initMocks(this);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAddPermission()
  {
    setAdminSubject();

    AssignedPermission sap = createPermission("trillian", false, "repository:*:READ");

    assertEquals("trillian", sap.getName());
    assertEquals("repository:*:READ", sap.getPermission().getValue());
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

    Collection<PermissionDescriptor> list = securitySystem.getAvailablePermissions();

    assertNotNull(list);
    assertThat(list).isNotEmpty();
  }

  /**
   * Method description
   *
   */
  @Test
  public void testDeletePermission()
  {
    setAdminSubject();

    AssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    securitySystem.deletePermission(sap);

    assertThat(securitySystem.getPermissions(p -> p.getName().equals("trillian"))).isEmpty();
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetAllPermissions()
  {
    setAdminSubject();

    AssignedPermission trillian = createPermission("trillian", false,
                                          "repository:*:READ");
    AssignedPermission dent = createPermission("dent", false,
                                      "repository:*:READ");
    AssignedPermission marvin = createPermission("marvin", false,
                                        "repository:*:READ");

    Collection<AssignedPermission> all = securitySystem.getPermissions(p -> true);

    assertEquals(3, all.size());
    assertThat(all).contains(trillian, dent, marvin);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetPermission()
  {
    setAdminSubject();

    AssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    Collection<AssignedPermission> other = securitySystem.getPermissions(p -> p.getName().equals("trillian"));

    assertThat(other).containsExactly(sap);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetPermissionsWithPredicate()
  {
    setAdminSubject();

    AssignedPermission trillian = createPermission("trillian", false,
                                          "repository:*:READ");
    AssignedPermission dent = createPermission("dent", false,
                                      "repository:*:READ");

    createPermission("hitchhiker", true, "repository:*:READ");

    Collection<AssignedPermission> filtered =
      securitySystem.getPermissions(p -> !p.isGroupPermission());

    assertThat(filtered)
      .hasSize(2)
      .contains(trillian, dent);
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

    AssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    setUserSubject();
    securitySystem.deletePermission(sap);
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
  private AssignedPermission createPermission(String name,
    boolean groupPermission, String value)
  {
    AssignedPermission ap = new AssignedPermission(name, groupPermission,
                              value);
    securitySystem.addPermission(ap);

    return securitySystem.getPermissions(permission -> Objects.equal(name, permission.getName())
      && Objects.equal(groupPermission, permission.isGroupPermission())
      && Objects.equal(value, permission.getPermission().getValue())).stream().findAny().orElseThrow(() -> new AssertionError("created permission not found"));
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
}

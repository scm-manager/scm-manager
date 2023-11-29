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

import com.google.common.base.Objects;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sonia.scm.AbstractTestBase;
import sonia.scm.auditlog.Auditor;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.JAXBConfigurationEntryStoreFactory;
import sonia.scm.store.StoreCacheConfigProvider;
import sonia.scm.util.ClassLoaders;
import sonia.scm.util.MockUtil;

import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultSecuritySystemTest extends AbstractTestBase
{

  private JAXBConfigurationEntryStoreFactory jaxbConfigurationEntryStoreFactory;
  private PluginLoader pluginLoader;
  @Mock
  private Auditor auditor;

  private DefaultSecuritySystem securitySystem;

  @Before
  public void createSecuritySystem()
  {
    jaxbConfigurationEntryStoreFactory =
      spy(new JAXBConfigurationEntryStoreFactory(contextProvider , repositoryLocationResolver, new UUIDKeyGenerator(), null, new StoreCacheConfigProvider(false)) {});
    pluginLoader = mock(PluginLoader.class);
    when(pluginLoader.getUberClassLoader()).thenReturn(ClassLoaders.getContextClassLoader(DefaultSecuritySystem.class));

    MockitoAnnotations.initMocks(this);
    securitySystem = new DefaultSecuritySystem(jaxbConfigurationEntryStoreFactory, pluginLoader, Set.of(auditor));
  }

  @Test
  public void testAddPermission()
  {
    setAdminSubject();

    AssignedPermission sap = createPermission("trillian", false, "repository:*:READ");

    assertEquals("trillian", sap.getName());
    assertEquals("repository:*:READ", sap.getPermission().getValue());
    assertEquals(false, sap.isGroupPermission());
  }

  @Test
  public void testAvailablePermissions()
  {
    setAdminSubject();

    Collection<PermissionDescriptor> list = securitySystem.getAvailablePermissions();

    assertNotNull(list);
    assertThat(list).isNotEmpty();
  }

  @Test
  public void testDeletePermission()
  {
    setAdminSubject();

    AssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    securitySystem.deletePermission(sap);

    assertThat(securitySystem.getPermissions(p -> p.getName().equals("trillian"))).isEmpty();
  }

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

  @Test
  public void testGetPermission()
  {
    setAdminSubject();

    AssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    Collection<AssignedPermission> other = securitySystem.getPermissions(p -> p.getName().equals("trillian"));

    assertThat(other).containsExactly(sap);
  }

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

  @Test(expected = UnauthorizedException.class)
  public void testUnauthorizedAddPermission()
  {
    setUserSubject();
    createPermission("trillian", false, "repository:*:READ");
  }

  @Test(expected = UnauthorizedException.class)
  public void testUnauthorizedDeletePermission()
  {
    setAdminSubject();

    AssignedPermission sap = createPermission("trillian", false,
                                     "repository:*:READ");

    setUserSubject();
    securitySystem.deletePermission(sap);
  }

  @Test
  public void shouldCallAuditorForNewUserPermission()
  {
    setAdminSubject();

    createPermission("trillian", false, "repository:*:READ");

    verify(auditor).createEntry(argThat(
      context -> {
        assertThat(context.getEntity()).isEqualTo("trillian");
        assertThat(context.getAdditionalLabels()).contains("user");
        assertThat(context.getOldObject()).isNull();
        assertThat(context.getObject())
          .extracting("permission")
          .extracting("value")
          .isEqualTo("repository:*:READ");
        return true;
      }
    ));
  }

  @Test
  public void shouldCallAuditorForNewGroupPermission()
  {
    setAdminSubject();

    createPermission("trillian", true, "repository:*:READ");

    verify(auditor).createEntry(argThat(
      context -> {
        assertThat(context.getEntity()).isEqualTo("trillian");
        assertThat(context.getAdditionalLabels()).contains("group");
        assertThat(context.getOldObject()).isNull();
        assertThat(context.getObject())
          .extracting("permission")
          .extracting("value")
          .isEqualTo("repository:*:READ");
        return true;
      }
    ));
  }

  @Test
  public void shouldCallAuditorForRemovedUserPermission()
  {
    setAdminSubject();

    createPermission("trillian", false, "repository:*:READ");
    reset(auditor);
    securitySystem.deletePermission(new AssignedPermission("trillian", false, "repository:*:READ"));

    verify(auditor).createEntry(argThat(
      context -> {
        assertThat(context.getEntity()).isEqualTo("trillian");
        assertThat(context.getAdditionalLabels()).contains("user");
        assertThat(context.getObject()).isNull();
        assertThat(context.getOldObject())
          .extracting("permission")
          .extracting("value")
          .isEqualTo("repository:*:READ");
        return true;
      }
    ));
  }

  @Test
  public void shouldCallAuditorForRemovedGroupPermission()
  {
    setAdminSubject();

    createPermission("trillian", true, "repository:*:READ");
    reset(auditor);
    securitySystem.deletePermission(new AssignedPermission("trillian", true, "repository:*:READ"));

    verify(auditor).createEntry(argThat(
      context -> {
        assertThat(context.getEntity()).isEqualTo("trillian");
        assertThat(context.getAdditionalLabels()).contains("group");
        assertThat(context.getObject()).isNull();
        assertThat(context.getOldObject())
          .extracting("permission")
          .extracting("value")
          .isEqualTo("repository:*:READ");
        return true;
      }
    ));
  }

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

  private void setAdminSubject()
  {
    setSubject(MockUtil.createAdminSubject());
  }

  private void setUserSubject()
  {
    org.apache.shiro.mgt.SecurityManager sm =
      new DefaultSecurityManager(new SimpleAccountRealm());

    setSubject(MockUtil.createUserSubject(sm));
  }
}

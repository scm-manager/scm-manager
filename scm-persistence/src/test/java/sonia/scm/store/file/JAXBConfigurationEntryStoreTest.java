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

package sonia.scm.store.file;


import com.google.common.io.Closeables;
import com.google.common.io.Resources;
import org.junit.Test;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.UUIDKeyGenerator;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.ConfigurationEntryStoreTestBase;
import sonia.scm.store.StoreObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class JAXBConfigurationEntryStoreTest
  extends ConfigurationEntryStoreTestBase
{

  private static final String RESOURCE_FIXED =
    "sonia/scm/store/fixed.format.xml";

  private static final String RESOURCE_WRONG =
    "sonia/scm/store/wrong.format.xml";



  @Test
  public void testLoad() throws IOException
  {
    ConfigurationEntryStore<AssignedPermission> store =
      createPermissionStore(RESOURCE_FIXED);
    AssignedPermission a1 = store.get("3ZOHKUePB3");

    assertEquals("tuser", a1.getName());

    AssignedPermission a2 = store.get("7COHL2j1G1");

    assertEquals("tuser2", a2.getName());

    AssignedPermission a3 = store.get("A0OHL3Qqw2");

    assertEquals("tuser3", a3.getName());
  }


  @Test
  public void testLoadWrongFormat() throws IOException
  {
    ConfigurationEntryStore<AssignedPermission> store =
      createPermissionStore(RESOURCE_WRONG);
    AssignedPermission a1 = store.get("3ZOHKUePB3");

    assertEquals("tuser", a1.getName());

    AssignedPermission a2 = store.get("7COHL2j1G1");

    assertEquals("tuser2", a2.getName());

    AssignedPermission a3 = store.get("A0OHL3Qqw2");

    assertEquals("tuser3", a3.getName());
  }

  /**
   *  Method description
   *
   *
   *  @throws IOException
   */
  @Test
  public void testStoreAndLoad() throws IOException
  {
    String name = UUID.randomUUID().toString();
    ConfigurationEntryStore<AssignedPermission> store = createPermissionStore(RESOURCE_FIXED, name);

    store.put("a45", new AssignedPermission("tuser4", "repository:create"));
    store = createConfigurationStoreFactory()
      .withType(AssignedPermission.class)
      .withName(name)
      .build();

    AssignedPermission ap = store.get("a45");

    assertNotNull(ap);
    assertEquals("tuser4", ap.getName());
    assertEquals("repository:create", ap.getPermission().getValue());
  }

 @Test
  public void shouldStoreAndLoadInRepository() throws IOException
  {
    repoStore.put("abc", new StoreObject("abc_value"));
    StoreObject storeObject = repoStore.get("abc");

    assertNotNull(storeObject);
    assertEquals("abc_value", storeObject.getValue());
  }

  
  @Override
  protected ConfigurationEntryStoreFactory createConfigurationStoreFactory()
  {
    return new JAXBConfigurationEntryStoreFactory(contextProvider, repositoryLocationResolver, new UUIDKeyGenerator(), null, new StoreCacheFactory(new StoreCacheConfigProvider(false)));
  }


  private void copy(String resource, String name) throws IOException
  {
    URL url = Resources.getResource(resource);
    File confdir = new File(contextProvider.getBaseDirectory(), "config");
    File file = new File(confdir, name.concat(".xml"));
    OutputStream output = null;

    try
    {
      output = new FileOutputStream(file);
      Resources.copy(url, output);
    }
    finally
    {
      Closeables.close(output, true);
    }
  }


  private ConfigurationEntryStore<AssignedPermission> createPermissionStore(
    String resource)
    throws IOException
  {
    return createPermissionStore(resource, null);
  }

  /**
   * Method description
   *
   *
   * @param resource
   * @param name
   *
   * @return
   *
   * @throws IOException
   */
  private ConfigurationEntryStore<AssignedPermission> createPermissionStore(
    String resource, String name)
    throws IOException
  {
    if (name == null)
    {
      name = UUID.randomUUID().toString();
    }

    copy(resource, name);
    return createConfigurationStoreFactory()
      .withType(AssignedPermission.class)
      .withName(name)
      .build();
  }
}

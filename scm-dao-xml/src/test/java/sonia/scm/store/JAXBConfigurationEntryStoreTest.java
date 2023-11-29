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

package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;
import com.google.common.io.Resources;
import org.junit.Test;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.UUIDKeyGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class JAXBConfigurationEntryStoreTest
  extends ConfigurationEntryStoreTestBase
{

  /** Field description */
  private static final String RESOURCE_FIXED =
    "sonia/scm/store/fixed.format.xml";

  /** Field description */
  private static final String RESOURCE_WRONG =
    "sonia/scm/store/wrong.format.xml";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected ConfigurationEntryStoreFactory  createConfigurationStoreFactory()
  {
    return new JAXBConfigurationEntryStoreFactory(contextProvider, repositoryLocationResolver, new UUIDKeyGenerator(), null, new StoreCacheConfigProvider(false));
  }

  /**
   * Method description
   *
   *
   * @param resource
   * @param name
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param resource
   *
   * @return
   *
   * @throws IOException
   */
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

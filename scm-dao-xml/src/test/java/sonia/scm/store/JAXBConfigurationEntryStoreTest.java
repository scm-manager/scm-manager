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



package sonia.scm.store;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;
import com.google.common.io.Resources;

import org.junit.Test;

import sonia.scm.security.AssignedPermission;
import sonia.scm.security.UUIDKeyGenerator;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;

import java.util.UUID;

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
   *  Method description
   *
   *
   *  @throws IOException
   */
  @Test
  public void testStoreAndLoad() throws IOException
  {
    String name = UUID.randomUUID().toString();
    ConfigurationEntryStore<AssignedPermission> store =
      createPermissionStore(RESOURCE_FIXED, name);

    store.put("a45", new AssignedPermission("tuser4", "repository:create"));
    store =
      createConfigurationStoreFactory().getStore(AssignedPermission.class,
        name);

    AssignedPermission ap = store.get("a45");

    assertNotNull(ap);
    assertEquals("tuser4", ap.getName());
    assertEquals("repository:create", ap.getPermission());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected ConfigurationEntryStoreFactory createConfigurationStoreFactory()
  {
    return new JAXBConfigurationEntryStoreFactory(new UUIDKeyGenerator(),
      contextProvider);
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

    return createConfigurationStoreFactory().getStore(AssignedPermission.class,
      name);
  }
}

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



package sonia.scm.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;
import static sonia.scm.it.RepositoryITUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import java.util.Collection;
import org.junit.Ignore;

import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientFactory;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class AnonymousAccessITCase
{

  /** Field description */
  private static final Permission PERMISSION_ANONYMOUS_WRITE =
    new Permission("anonymous", PermissionType.WRITE);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryType
   */
  public AnonymousAccessITCase(String repositoryType)
  {
    this.repositoryType = repositoryType;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Parameters
  public static Collection<String[]> createParameters()
  {
    return createRepositoryTypeParameters();
  }

  /**
   * Method description
   *
   */
  @AfterClass
  public static void unsetAnonymousAccess()
  {
    toggleAnonymousAccess(false);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @BeforeClass
  public static void setAnonymousAccess()
  {
    toggleAnonymousAccess(true);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param anonymousAccess
   */
  private static void toggleAnonymousAccess(boolean anonymousAccess)
  {
    Client client = createAdminClient();
    WebResource resource = createResource(client, "config");
    ScmConfiguration config = resource.get(ScmConfiguration.class);

    assertNotNull(config);
    config.setAnonymousAccessEnabled(anonymousAccess);

    ClientResponse response = resource.post(ClientResponse.class, config);

    assertNotNull(response);
    assertEquals(201, response.getStatus());
    logoutClient(client);
  }

  /**
   * Method description
   *
   */
  @Before
  public void createTestRepository()
  {
    Client client = createAdminClient();

    repository = RepositoryTestData.createHeartOfGold(repositoryType);
    repository.setPublicReadable(true);
    repository = createRepository(client, repository);
    logoutClient(client);
  }

  /**
   * Method description
   *
   */
  @After
  public void removeTestRepository()
  {
    Client client = createAdminClient();

    deleteRepository(client, repository.getId());
    logoutClient(client);
  }

  /**
   * Method description
   *
   * @throws IOException
   */
  @Test
  @Ignore
  public void testAllowedAnonymousPush() throws IOException
  {
    Client client = createAdminClient();
    WebResource resource = createResource(client,
                             "repository/".concat(repository.getId()));

    repository.setPermissions(Arrays.asList(PERMISSION_ANONYMOUS_WRITE));
    resource.post(ClientResponse.class, repository);

    RepositoryClient repositoryClient = createAnonymousRepositoryClient();

    createRandomFile(repositoryClient);
    commit(repositoryClient, "added test files");
  }

  /**
   * Method description
   * 
    * TODO fix test case
   *
   * @throws IOException
   */
  @Test @Ignore
  public void testAnonymousClone() throws IOException
  {
    testSimpleAdminPush();

    RepositoryClient client = createAnonymousRepositoryClient();

    // client.checkout();
  }

  /**
   * Method description
   *
   * @throws IOException
   */
  @Ignore
  @Test(expected = IOException.class)
  public void testDeniedAnonymousPush() throws IOException
  {
    RepositoryClient repositoryClient = createAnonymousRepositoryClient();

    createRandomFile(repositoryClient);
    commit(repositoryClient, "added anonymous test file");
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testSimpleAdminPush() throws IOException
  {
    RepositoryClient repositoryClient = createAdminRepositoryClient();

    createRandomFile(repositoryClient);
    commit(repositoryClient, "added random file");
  }

  private RepositoryClient createAdminRepositoryClient() throws IOException {
    return createRepositoryClient(ADMIN_USERNAME, ADMIN_PASSWORD);
  }

  private RepositoryClient createAnonymousRepositoryClient() throws IOException {
    return createRepositoryClient(null, null);
  }

  private RepositoryClient createRepositoryClient(String username, String password) throws IOException {
    File directory = temporaryFolder.newFolder();
    String remoteUrl = repository.createUrl(BASE_URL);

    RepositoryClientFactory factory = new RepositoryClientFactory();
    return factory.create(repositoryType, remoteUrl, username, password, directory);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  /** Field description */
  private Repository repository;

  /** Field description */
  private final String repositoryType;
}

/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.client.RepositoryClient;
import sonia.scm.repository.client.RepositoryClientException;
import sonia.scm.repository.client.RepositoryClientFactory;

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;
import static sonia.scm.it.RepositoryITUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.File;
import java.io.IOException;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class AnonymousAccessITCase
{

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
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test
  public void testSimpleAdminPush()
          throws RepositoryClientException, IOException
  {
    File directory = temporaryFolder.newFolder();
    RepositoryClient client =
      RepositoryClientFactory.createClient(repositoryType, directory,
        repository.getUrl(), ADMIN_USERNAME, ADMIN_PASSWORD);

    client.init();
    createRandomFile(client);
    client.commit("added random file");
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  /** Field description */
  private Repository repository;

  /** Field description */
  private String repositoryType;
}

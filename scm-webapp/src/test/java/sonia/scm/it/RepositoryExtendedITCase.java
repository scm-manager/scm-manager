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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.client.RepositoryClient;
import sonia.scm.repository.client.RepositoryClientException;
import sonia.scm.repository.client.RepositoryClientFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.IOUtil;

import static sonia.scm.it.IntegrationTestUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class RepositoryExtendedITCase
{

  /**
   * Constructs ...
   *
   *
   *
   * @param repository
   * @param username
   * @param password
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  public RepositoryExtendedITCase(Repository repository, String username,
                                  String password)
          throws RepositoryClientException, IOException
  {
    this.repository = repository;
    this.username = username;
    this.password = password;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @AfterClass
  public static void cleanup()
  {
    Client client = createAdminClient();

    createResource(client, "users/trillian").delete();

    Collection<Repository> repositories =
      createResource(client, "repositories").get(
          new GenericType<Collection<Repository>>() {}
    );

    if (repositories != null)
    {
      for (Repository r : repositories)
      {
        createResource(client, "repositories/" + r.getId()).delete();
      }
    }

    client.destroy();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Parameters
  public static Collection<Object[]> createParameters()
  {
    Collection<Object[]> params = new ArrayList<Object[]>();
    Repository gitRepository = createRepository("git", "trillian");

    params.add(new Object[] { gitRepository, "trillian", "secret" });

    Repository svnRepository = createRepository("svn", "trillian");

    params.add(new Object[] { svnRepository, "trillian", "secret" });

    if (IOUtil.search("hg") != null)
    {
      Repository hgRepository = createRepository("hg", "trillian");

      params.add(new Object[] { hgRepository, "trillian", "secret" });
    }

    return params;
  }

  /**
   * Method description
   *
   */
  @BeforeClass
  public static void setup()
  {
    Client client = createAdminClient();
    User trillian = UserTestData.createTrillian();

    trillian.setPassword("secret");
    createResource(client, "users").post(trillian);
    client.destroy();
  }

  /**
   * Method description
   *
   *
   *
   * @param repository
   * @param username
   * @param password
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  private static void addTestFiles(Repository repository, String username,
                                   String password)
          throws RepositoryClientException, IOException
  {
    File directory = createTempDirectory();

    try
    {
      RepositoryClient rc =
        RepositoryClientFactory.createClient(repository.getType(), directory,
          repository.getUrl(), username, password);

      rc.init();

      for (int i = 0; i < 5; i++)
      {
        createRandomFile(rc, directory);
      }

      rc.commit("added some test files");
    }
    finally
    {
      IOUtil.delete(directory);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param client
   * @param directory
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  private static void createRandomFile(RepositoryClient client, File directory)
          throws IOException, RepositoryClientException
  {
    String uuid = UUID.randomUUID().toString();
    String name = "file-" + uuid + ".uuid";
    FileOutputStream out = null;

    try
    {
      out = new FileOutputStream(new File(directory, name));
      out.write(uuid.getBytes());
    }
    finally
    {
      IOUtil.close(out);
    }

    client.add(name);
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param username
   *
   * @return
   */
  private static Repository createRepository(String type, String username)
  {
    Client client = createAdminClient();
    Repository repository = RepositoryTestData.createHeartOfGold(type);

    repository.setPermissions(Arrays.asList(new Permission(username,
            PermissionType.WRITE)));

    ClientResponse response = createResource(client,
                                "repositories").post(ClientResponse.class,
                                  repository);
    String url = response.getHeaders().get("Location").get(0) + EXTENSION;

    response.close();
    repository = client.resource(url).get(Repository.class);
    client.destroy();

    return repository;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private static File createTempDirectory()
  {
    File directory = new File(System.getProperty("java.io.tmpdir"),
                              UUID.randomUUID().toString());

    IOUtil.mkdirs(directory);

    return directory;
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test
  public void read() throws RepositoryClientException, IOException
  {
    File directory = createTempDirectory();

    try
    {
      RepositoryClient rc =
        RepositoryClientFactory.createClient(repository.getType(), directory,
          repository.getUrl(), username, password);

      rc.init();
      rc.checkout();
    }
    finally
    {
      IOUtil.delete(directory);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Test
  public void write() throws RepositoryClientException, IOException
  {
    addTestFiles(repository, username, password);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String password;

  /** Field description */
  private Repository repository;

  /** Field description */
  private String username;
}

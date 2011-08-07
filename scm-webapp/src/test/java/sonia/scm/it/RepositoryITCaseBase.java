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
import org.junit.runners.Parameterized.Parameters;

import sonia.scm.ScmState;
import sonia.scm.Type;
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

import static org.junit.Assert.*;

import static sonia.scm.it.IntegrationTestUtil.*;
import static sonia.scm.it.RepositoryITUtil.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryITCaseBase
{

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param owner
   * @param write
   * @param read
   * @param noperm
   * @param password
   */
  public RepositoryITCaseBase(Repository repository, User owner, User write,
                              User read, User noperm, String password)
  {
    this.repository = repository;
    this.ownerUser = owner;
    this.writeUser = write;
    this.readUser = read;
    this.nopermUser = noperm;
    this.password = password;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param client
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  public static void addTestFiles(RepositoryClient client)
          throws RepositoryClientException, IOException
  {
    for (int i = 0; i < 5; i++)
    {
      createRandomFile(client);
    }

    client.commit("added some test files");
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
  public static void addTestFiles(Repository repository, String username,
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
      addTestFiles(rc);
    }
    finally
    {
      IOUtil.delete(directory);
    }
  }

  /**
   * Method description
   *
   */
  @AfterClass
  public static void cleanup()
  {
    Client client = createAdminClient();

    deleteUser(client, UserTestData.createTrillian());
    deleteUser(client, UserTestData.createZaphod());
    deleteUser(client, UserTestData.createMarvin());
    deleteUser(client, UserTestData.createPerfect());

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
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  @Parameters
  public static Collection<Object[]> createParameters()
          throws RepositoryClientException, IOException
  {
    Client client = createClient();
    ScmState state = authenticateAdmin(client);

    assertNotNull(state);
    assertTrue(state.isSuccess());

    Collection<Object[]> params = new ArrayList<Object[]>();
    User owner = UserTestData.createTrillian();

    createUser(owner);

    User write = UserTestData.createZaphod();

    createUser(write);

    User read = UserTestData.createMarvin();

    createUser(read);

    User noperm = UserTestData.createPerfect();

    createUser(noperm);

    for (Type t : state.getRepositoryTypes())
    {
      if (t.getName().equals("git"))
      {
        Repository gitRepository = createTestRepository("git", owner, write,
                                     read);

        params.add(new Object[]
        {
          gitRepository, owner, write, read, noperm, "secret"
        });
      }
      else if (t.getName().equals("svn"))
      {
        Repository svnRepository = createTestRepository("svn", owner, write,
                                     read);

        params.add(new Object[]
        {
          svnRepository, owner, write, read, noperm, "secret"
        });
      }
      else if (t.getName().equals("hg"))
      {
        Repository hgRepository = createTestRepository("hg", owner, write,
                                    read);

        params.add(new Object[]
        {
          hgRepository, owner, write, read, noperm, "secret"
        });
      }
    }

    return params;
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param owner
   * @param write
   * @param read
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryClientException
   */
  private static Repository createTestRepository(String type, User owner,
          User write, User read)
          throws RepositoryClientException, IOException
  {
    Client client = createAdminClient();
    Repository repository = RepositoryTestData.createHeartOfGold(type);

    //J-
    repository.setPermissions(Arrays.asList(
          new Permission(owner.getName(), PermissionType.OWNER), 
          new Permission(write.getName(), PermissionType.WRITE),
          new Permission(read.getName(), PermissionType.READ))
    );
    //J+
    repository = createRepository(client, repository);
    client.destroy();
    addTestFiles(repository, ADMIN_USERNAME, ADMIN_PASSWORD);

    return repository;
  }

  /**
   * Method description
   *
   *
   * @param user
   */
  private static void createUser(User user)
  {
    Client client = createAdminClient();

    user.setPassword("secret");
    createResource(client, "users").post(user);
    client.destroy();
  }

  /**
   * Method description
   *
   *
   * @param client
   * @param user
   */
  private static void deleteUser(Client client, User user)
  {
    createResource(client, "users/".concat(user.getName())).delete();
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param directory
   *
   * @return
   *
   * @throws RepositoryClientException
   */
  protected RepositoryClient createRepositoryClient(User user, File directory)
          throws RepositoryClientException
  {
    return RepositoryClientFactory.createClient(repository.getType(),
            directory, repository.getUrl(), user.getName(), password);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected User nopermUser;

  /** Field description */
  protected User ownerUser;

  /** Field description */
  protected String password;

  /** Field description */
  protected User readUser;

  /** Field description */
  protected Repository repository;

  /** Field description */
  protected User writeUser;
}

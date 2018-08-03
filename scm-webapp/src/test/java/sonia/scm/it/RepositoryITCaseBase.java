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
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static sonia.scm.it.IntegrationTestUtil.ADMIN_PASSWORD;
import static sonia.scm.it.IntegrationTestUtil.ADMIN_USERNAME;
import static sonia.scm.it.IntegrationTestUtil.commit;
import static sonia.scm.it.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.IntegrationTestUtil.createRandomFile;
import static sonia.scm.it.IntegrationTestUtil.createRepositoryTypeParameters;
import static sonia.scm.it.IntegrationTestUtil.createResource;
import static sonia.scm.it.IntegrationTestUtil.createTempDirectory;
import static sonia.scm.it.IntegrationTestUtil.readJson;
import static sonia.scm.it.RepositoryITUtil.createUrl;
import static sonia.scm.it.UserITUtil.postUser;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryITCaseBase
{

  private static final Collection<RepositoryDto> CREATED_REPOSITORIES = new ArrayList<>();

  protected User nopermUser;
  protected User ownerUser;
  protected String password;
  protected User readUser;
  protected RepositoryDto repository;
  protected User writeUser;

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
  public RepositoryITCaseBase(RepositoryDto repository, User owner, User write,
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
   */
  public static void addTestFiles(RepositoryClient client) throws IOException
  {
    for (int i = 0; i < 5; i++)
    {
      createRandomFile(client);
    }

    commit(client, "added some test files");
  }

  /**
   * Method description
   *
   * @param repository
   * @param username
   * @param password
   *
   * @throws IOException
   */
  public static void addTestFiles(RepositoryDto repository, String username,
    String password)
  {
    File directory = createTempDirectory();

    try {
      RepositoryClientFactory clientFactory = new RepositoryClientFactory();
      RepositoryClient client = clientFactory.create(
        repository.getType(), createUrl(repository),
        username, password, directory
      );

      addTestFiles(client);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        IOUtil.delete(directory);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Method description
   *
   */
  @AfterClass
  public static void cleanup()
  {
    ScmClient client = createAdminClient();

    deleteUser(client, UserTestData.createTrillian());
    deleteUser(client, UserTestData.createZaphod());
    deleteUser(client, UserTestData.createMarvin());
    deleteUser(client, UserTestData.createPerfect());

    for (RepositoryDto r : CREATED_REPOSITORIES)
    {
      createResource(client, "repositories/" + r.getNamespace() + "/"  + r.getName()).delete();
    }
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @Parameters(name = "{6}")
  public static Collection<Object[]> createParameters() throws IOException
  {
    Collection<Object[]> params = new ArrayList<>();
    User owner = UserTestData.createTrillian();

    createUser(owner);

    User write = UserTestData.createZaphod();

    createUser(write);

    User read = UserTestData.createMarvin();

    createUser(read);

    User noperm = UserTestData.createPerfect();

    createUser(noperm);
    createRepositoryTypeParameters().forEach(t -> appendTestParameter(params, t, owner, write, read, noperm));

    return params;
  }

  /**
   * Method description
   *
   *
   * @param params
   * @param type
   * @param owner
   * @param write
   * @param read
   * @param noperm
   *
   * @throws IOException
   */
  private static void appendTestParameter(Collection<Object[]> params,
    String type, User owner, User write, User read, User noperm)
  {
    RepositoryDto repository = createTestRepository(type, owner, write, read);
    params.add(new Object[]
      {
        repository, owner, write, read, noperm, "secret", repository.getType() + "-" + owner.getId()
      });
  }

  /**
   * Method description
   *
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
   */
  private static RepositoryDto createTestRepository(String type,
    User owner, User write, User read)
  {
    ScmClient client = createAdminClient();

    // TODO Activate for tests when implemented
//    repository.setPermissions(Arrays.asList(
//      new Permission(owner.getName(), PermissionType.OWNER),
//      new Permission(write.getName(), PermissionType.WRITE),
//      new Permission(read.getName(), PermissionType.READ))
//    );
    String repositoryJson = readJson("repository-" + type + ".json");
    RepositoryDto repository = RepositoryITUtil.createRepository(client, repositoryJson);

    CREATED_REPOSITORIES.add(repository);

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
    ScmClient client = createAdminClient();

    user.setPassword("secret");

    postUser(client, user);
  }

  /**
   * Method description
   *
   *
   * @param client
   * @param user
   */
  private static void deleteUser(ScmClient client, User user)
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
   * @throws IOException
   */
  protected RepositoryClient createRepositoryClient(User user, File directory) throws IOException
  {
    RepositoryClientFactory clientFactory = new RepositoryClientFactory();
    return clientFactory.create(repository.getType(), createUrl(repository),
      user.getName(), password, directory);
  }
}

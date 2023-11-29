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

package sonia.scm.it.webapp;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import jakarta.ws.rs.client.Invocation;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.debug.DebugHookData;
import sonia.scm.it.utils.TestData;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static sonia.scm.it.webapp.IntegrationTestUtil.createResource;
import static sonia.scm.it.webapp.IntegrationTestUtil.readJson;
import static sonia.scm.it.webapp.RepositoryITUtil.createRepository;
import static sonia.scm.it.webapp.RepositoryITUtil.deleteRepository;

/**
 * Integration tests for repository hooks.
 *
 * @author Sebastian Sdorra
 */
@RunWith(Parameterized.class)
public class RepositoryHookITCase extends AbstractAdminITCaseBase
{

  private static final long WAIT_TIME = 125;

  private static final RepositoryClientFactory REPOSITORY_CLIENT_FACTORY = new RepositoryClientFactory();

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private final String repositoryType;
  private RepositoryDto repository;
  private File workingCopy;
  private RepositoryClient repositoryClient;

  /**
   * Constructs a new instance with a repository type.
   *
   * @param repositoryType repository type
   */
  public RepositoryHookITCase(String repositoryType)
  {
    this.repositoryType = repositoryType;
  }

  @BeforeClass
  public static void cleanup() {
    TestData.cleanup();
  }

  /**
   * Creates a test repository.
   *
   * @throws IOException
   */
  @Before
  public void setUpTestRepository() throws IOException
  {
    repository = createRepository(client, readJson("repository-" + repositoryType + ".json"));
    workingCopy = tempFolder.newFolder();
    repositoryClient = createRepositoryClient();
  }

  /**
   * Removes the tests repository.
   */
  @After
  public void removeTestRepository()
  {
    if (repository != null) {
      deleteRepository(client, repository);
    }
  }

  /**
   * Tests that the debug service has received the commit.
   *
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testSimpleHook() throws IOException, InterruptedException
  {
    // commit and push commit
    Files.write("a", new File(workingCopy, "a.txt"), Charsets.UTF_8);
    repositoryClient.getAddCommand().add("a.txt");
    Changeset changeset = commit("added a");

    // wait some time, because the debug hook is asnychron
    Thread.sleep(WAIT_TIME);

    // check debug servlet for pushed commit
    Invocation.Builder wr = createResource(client, "../debug/" + repository.getNamespace() + "/" + repository.getName() + "/post-receive/last");
    DebugHookData data = wr.buildGet().invoke(DebugHookData.class);
    assertNotNull(data);
    assertThat(data.getChangesets(), contains(changeset.getId()));
  }

  /**
   * Tests that the debug service receives only new commits.
   *
   * @throws IOException
   * @throws InterruptedException
   */
  @Test
  public void testOnlyNewCommit() throws IOException, InterruptedException
  {
    // skip test if branches are not supported by repository type
    Assume.assumeTrue(repositoryClient.isCommandSupported(ClientCommand.BRANCH));

    // push commit
    Files.write("a", new File(workingCopy, "a.txt"), Charsets.UTF_8);
    repositoryClient.getAddCommand().add("a.txt");
    Changeset a = commit("added a");

    // create branch
    repositoryClient.getBranchCommand().branch("feature/added-b");

    // commit and push again
    Files.write("b", new File(workingCopy, "b.txt"), Charsets.UTF_8);
    repositoryClient.getAddCommand().add("a.txt");
    Changeset b = commit("added b");

    // wait some time, because the debug hook is asnychron
    Thread.sleep(WAIT_TIME);

    // check debug servlet that only one commit is present
    Invocation.Builder wr = createResource(client, String.format("../debug/%s/%s/post-receive/last", repository.getNamespace(), repository.getName()));
    DebugHookData data = wr.buildGet().invoke(DebugHookData.class);
    assertNotNull(data);
    assertThat(data.getChangesets(), allOf(
      contains(b.getId()),
      not(
        contains(a.getId())
      )
    ));
  }

  private Changeset commit(String message) throws IOException {
    Changeset a = repositoryClient.getCommitCommand().commit(
      new Person("scmadmin", "scmadmin@scm-manager.org"), message
    );
    if ( repositoryClient.isCommandSupported(ClientCommand.PUSH) ) {
      repositoryClient.getPushCommand().push();
    }
    return a;
  }

  private RepositoryClient createRepositoryClient() throws IOException
  {
    return REPOSITORY_CLIENT_FACTORY.create(repositoryType,
      String.format("%srepo/%s/%s", IntegrationTestUtil.BASE_URL, repository.getNamespace(), repository.getName()),
      IntegrationTestUtil.ADMIN_USERNAME, IntegrationTestUtil.ADMIN_PASSWORD, workingCopy
    );
  }


  /**
   * Returns repository types a test parameter.
   *
   * @return repository types test parameter
   */
  @Parameters(name = "{0}")
  public static Collection<String[]> createParameters()
  {
    return IntegrationTestUtil.createRepositoryTypeParameters();
  }

}

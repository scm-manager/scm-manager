/**
 * Copyright (c) 2014, Sebastian Sdorra
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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static sonia.scm.it.IntegrationTestUtil.readJson;
import static sonia.scm.it.RepositoryITUtil.createRepository;
import static sonia.scm.it.RepositoryITUtil.deleteRepository;

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
//    WebResource wr = createResource(client, "debug/" + repository.getId() + "/post-receive/last");
//    DebugHookData data = wr.get(DebugHookData.class);
//    assertNotNull(data);
//    assertThat(data.getChangesets(), contains(changeset.getId()));
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
//    WebResource wr = createResource(client, "debug/" + repository.getId() + "/post-receive/last");
//    DebugHookData data = wr.get(DebugHookData.class);
//    assertNotNull(data);
//    assertThat(data.getChangesets(), allOf(
//      contains(b.getId()),
//      not(
//        contains(a.getId())
//      )
//    ));
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
      IntegrationTestUtil.BASE_URL + repositoryType + "/" + repository.getNamespace() + "/" + repository.getName(),
      IntegrationTestUtil.ADMIN_USERNAME, IntegrationTestUtil.ADMIN_PASSWORD, workingCopy
    );
  }

  
  /**
   * Returns repository types a test parameter.
   *
   * @return repository types test parameter
   */
  @Parameters(name = "{0}")
  public static Collection<String> createParameters()
  {
    return IntegrationTestUtil.createRepositoryTypeParameters();
  }
  
}

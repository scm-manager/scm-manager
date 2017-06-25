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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sun.jersey.api.client.Client;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static sonia.scm.it.IntegrationTestUtil.*;
import static sonia.scm.it.RepositoryITUtil.*;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientFactory;

/**
 * Integration test for RepositoryPathMatching with ".git" and without ".git".
 * 
 * @author Sebastian Sdorra
 * @since 1.54
 */
public class GitRepositoryPathMatcherITCase {
  
  private static final RepositoryClientFactory REPOSITORY_CLIENT_FACTORY = new RepositoryClientFactory();
  
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  
  private Client apiClient;
  private Repository repository;
  
  @Before
  public void setUp() {
    apiClient = createAdminClient();
    Repository testRepository = RepositoryTestData.createHeartOfGold("git");
    this.repository = createRepository(apiClient, testRepository);
  }
  
  @After
  public void tearDown() {
    deleteRepository(apiClient, repository.getId());
  }
  
  // tests begin
  
  @Test
  public void testWithoutDotGit() throws IOException {
    String urlWithoutDotGit = createUrl();
    cloneAndPush(urlWithoutDotGit);
  }
  
  @Test
  public void testWithDotGit() throws IOException {
    String urlWithDotGit = createUrl() + ".git";
    cloneAndPush(urlWithDotGit);
  }
  
  // tests end
  
  private String createUrl() {
    return BASE_URL + "git/" + repository.getName();
  }
  
  private void cloneAndPush( String url ) throws IOException {
    cloneRepositoryAndPushFiles(url);
    cloneRepositoryAndCheckFiles(url);
  }
  
  private void cloneRepositoryAndPushFiles(String url) throws IOException {
    RepositoryClient repositoryClient = createRepositoryClient(url);
    
    Files.write("a", new File(repositoryClient.getWorkingCopy(), "a.txt"), Charsets.UTF_8);
    repositoryClient.getAddCommand().add("a.txt");
    commit(repositoryClient, "added a");
    
    Files.write("b", new File(repositoryClient.getWorkingCopy(), "b.txt"), Charsets.UTF_8);
    repositoryClient.getAddCommand().add("b.txt");
    commit(repositoryClient, "added b");
  }
  
  private void cloneRepositoryAndCheckFiles(String url) throws IOException {
    RepositoryClient repositoryClient = createRepositoryClient(url);
    File workingCopy = repositoryClient.getWorkingCopy();
    
    File a = new File(workingCopy, "a.txt");
    assertTrue(a.exists());
    assertEquals("a", Files.toString(a, Charsets.UTF_8));
    
    File b = new File(workingCopy, "b.txt");
    assertTrue(b.exists());
    assertEquals("b", Files.toString(b, Charsets.UTF_8));
  }
  
   private void commit(RepositoryClient repositoryClient, String message) throws IOException {
    repositoryClient.getCommitCommand().commit(
      new Person("scmadmin", "scmadmin@scm-manager.org"), message
    );
    if ( repositoryClient.isCommandSupported(ClientCommand.PUSH) ) {
      repositoryClient.getPushCommand().push();
    }
  }
  
  private RepositoryClient createRepositoryClient(String url) throws IOException {
    return REPOSITORY_CLIENT_FACTORY.create("git", url, ADMIN_USERNAME, ADMIN_PASSWORD, tempFolder.newFolder());
  }
}

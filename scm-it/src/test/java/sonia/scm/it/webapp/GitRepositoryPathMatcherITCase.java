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
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.api.v2.resources.RepositoryDto;
import sonia.scm.it.utils.TestData;
import sonia.scm.repository.Person;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static sonia.scm.it.webapp.IntegrationTestUtil.ADMIN_PASSWORD;
import static sonia.scm.it.webapp.IntegrationTestUtil.ADMIN_USERNAME;
import static sonia.scm.it.webapp.IntegrationTestUtil.BASE_URL;
import static sonia.scm.it.webapp.IntegrationTestUtil.createAdminClient;
import static sonia.scm.it.webapp.IntegrationTestUtil.readJson;
import static sonia.scm.it.webapp.RepositoryITUtil.createRepository;
import static sonia.scm.it.webapp.RepositoryITUtil.deleteRepository;

/**
 * Integration test for RepositoryPathMatching with ".git" and without ".git".
 *
 * @since 1.54
 */
public class GitRepositoryPathMatcherITCase {

  private static final RepositoryClientFactory REPOSITORY_CLIENT_FACTORY = new RepositoryClientFactory();

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private ScmClient apiClient;
  private RepositoryDto repository;

  @BeforeClass
  public static void cleanup() {
    TestData.cleanup();
  }

  @Before
  public void setUp() {
    apiClient = createAdminClient();
    this.repository = createRepository(apiClient, readJson("repository-git.json"));
  }

  @After
  public void tearDown() {
    deleteRepository(apiClient, repository);
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
    return BASE_URL + "repo/" + repository.getNamespace() + "/" + repository.getName();
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

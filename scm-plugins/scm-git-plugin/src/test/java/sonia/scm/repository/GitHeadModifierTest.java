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
    
package sonia.scm.repository;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitHeadModifierTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock
  private GitRepositoryHandler repositoryHandler;

  @InjectMocks
  private GitHeadModifier modifier;

  @Test
  public void testEnsure() throws IOException, GitAPIException {
    Repository repository = RepositoryTestData.createHeartOfGold("git");
    File headFile = create(repository, "master");

    boolean result = modifier.ensure(repository, "develop");

    assertEquals("ref: refs/heads/develop", Files.readFirstLine(headFile, Charsets.UTF_8));
    assertTrue(result);
  }

  @Test
  public void testEnsureWithSameBranch() throws IOException, GitAPIException {
    Repository repository = RepositoryTestData.createHeartOfGold("git");
    create(repository, "develop");

    boolean result = modifier.ensure(repository, "develop");

    assertFalse(result);
  }

  private File create(Repository repository, String head) throws IOException, GitAPIException {
    File directory = temporaryFolder.newFolder();

    Git.init()
      .setBare(true)
      .setDirectory(directory)
      .call();

    File headFile = new File(directory, "HEAD");
    Files.write(String.format("ref: refs/heads/%s\n", head), headFile, Charsets.UTF_8);

    when(repositoryHandler.getDirectory(repository.getId())).thenReturn(directory);

    return headFile;
  }

}

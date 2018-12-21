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
import org.mockito.runners.MockitoJUnitRunner;

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

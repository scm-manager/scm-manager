/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

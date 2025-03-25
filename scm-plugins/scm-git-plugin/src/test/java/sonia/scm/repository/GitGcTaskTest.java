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

import com.google.common.collect.Lists;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GitGcTask}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class GitGcTaskTest
{

  @Mock
  private RepositoryManager manager;
  
  @Mock
  private RepositoryDirectoryHandler handler;
  
  @Mock
  private GarbageCollectCommand gcc;
  
  @Mock
  private Git git;

  private GitGcTask task;
  
  /**
   * Setup mocks for tests.
   * 
   * @throws GitAPIException 
   */
  @Before
  public void setUp() throws GitAPIException
  {
    when(git.gc()).thenReturn(gcc);
    when(gcc.getStatistics()).thenReturn(new Properties());
    when(gcc.call()).thenReturn(new Properties());
    when(manager.getHandler(GitRepositoryHandler.TYPE_NAME)).thenReturn(handler);
    task = new GitGcTask(manager){
      
      @Override
      protected Git open(File file) throws IOException
      {
        return git;
      }
      
    };
  }

  /**
   * Tests {@link GitGcTask#run()}.
   * 
   * @throws GitAPIException 
   */
  @Test
  public void testRun() throws GitAPIException
  {
    // prepare repositories for task
    Repository unhealthy = mock(Repository.class);
    when(unhealthy.getType()).thenReturn("git");

    Repository invalid = mock(Repository.class);
    when(unhealthy.getType()).thenReturn("git");
    when(unhealthy.isValid()).thenReturn(Boolean.FALSE);
    
    List<Repository> repositories = Lists.newArrayList(
        RepositoryTestData.create42Puzzle("git"),
        RepositoryTestData.createHeartOfGold("hg"),
        unhealthy,
        invalid
    );
    when(manager.getAll()).thenReturn(repositories);
    
    // run
    task.run();
    
    // gc command should only be called once
    verify(gcc).getStatistics();
    verify(gcc).call();
  }

}

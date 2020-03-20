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
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
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

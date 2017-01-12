/***
 * Copyright (c) 2015, Sebastian Sdorra
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
 * https://bitbucket.org/sdorra/scm-manager
 * 
 */

package sonia.scm.repository;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    when(unhealthy.isHealthy()).thenReturn(Boolean.FALSE);
    
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
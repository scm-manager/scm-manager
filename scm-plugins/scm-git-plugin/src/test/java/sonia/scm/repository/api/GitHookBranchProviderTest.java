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

package sonia.scm.repository.api;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.hamcrest.Matchers;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link GitHookBranchProvider}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHookBranchProviderTest {

  @Mock
  private ReceiveCommand command;
  
  private List<ReceiveCommand> commands;
  
  /**
   * Prepare mocks for upcoming test.
   */
  @Before
  public void setUpMocks(){
    commands = Lists.newArrayList(command);
  }
  
  /**
   * Tests {@link GitHookBranchProvider#getCreatedOrModified()}.
   */
  @Test
  public void testGetCreatedOrModified(){
    List<ReceiveCommand.Type> types = Arrays.asList( 
      ReceiveCommand.Type.CREATE, ReceiveCommand.Type.UPDATE, ReceiveCommand.Type.UPDATE_NONFASTFORWARD 
    );
    for ( ReceiveCommand.Type type : types ){
      checkCreatedOrModified(type);
    }
  }
  
  private void checkCreatedOrModified(ReceiveCommand.Type type){
    GitHookBranchProvider provider = createGitHookBranchProvider(type, "refs/heads/hello");
    assertThat(provider.getCreatedOrModified(), Matchers.contains("hello"));
    assertThat(provider.getDeletedOrClosed(), empty());    
  }

  
  /**
   * Tests {@link GitHookBranchProvider#getDeletedOrClosed()}.
   */  
  @Test
  public void testGetDeletedOrClosed(){
    GitHookBranchProvider provider = createGitHookBranchProvider(ReceiveCommand.Type.DELETE, "refs/heads/hello");
    assertThat(provider.getDeletedOrClosed(), Matchers.contains("hello"));
    assertThat(provider.getCreatedOrModified(), empty());
  }
  
  /**
   * Tests {@link GitHookBranchProvider} with a tag instead of a branch.
   */
  @Test
  public void testWithTag(){
    GitHookBranchProvider provider = createGitHookBranchProvider(ReceiveCommand.Type.CREATE, "refs/tags/1.0.0");
    assertThat(provider.getCreatedOrModified(), empty());
    assertThat(provider.getDeletedOrClosed(), empty());
  }
  
  private GitHookBranchProvider createGitHookBranchProvider(ReceiveCommand.Type type, String refName){
    when(command.getType()).thenReturn(type);
    when(command.getRefName()).thenReturn(refName);
    return new GitHookBranchProvider(commands);
  }

}
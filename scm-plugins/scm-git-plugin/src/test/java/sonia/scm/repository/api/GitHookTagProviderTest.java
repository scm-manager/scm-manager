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
import java.util.List;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import sonia.scm.repository.Tag;

/**
 * Unit tests for {@link GitHookTagProvider}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHookTagProviderTest {

  @Mock
  private ReceiveCommand command;
  
  private List<ReceiveCommand> commands;
  
  /**
   * Set up mocks for upcoming tests.
   */
  @Before
  public void setUpMocks(){
    commands = Lists.newArrayList(command);
  }

  /**
   * Tests {@link GitHookTagProvider#getCreatedTags()}.
   */
  @Test
  public void testGetCreatedTags() {
    String revision = "b2002b64013e54b78eac251df0672bd5d6a83aa7";
    GitHookTagProvider provider = createProvider(ReceiveCommand.Type.CREATE, "refs/tags/1.0.0", revision);
    
    assertTag("1.0.0", revision, provider.getCreatedTags());
    assertThat(provider.getDeletedTags(), empty());
  }
  
  /**
   * Tests {@link GitHookTagProvider#getDeletedTags()}.
   */
  @Test
  public void testGetDeletedTags() {
    String revision = "b2002b64013e54b78eac251df0672bd5d6a83aa7";
    GitHookTagProvider provider = createProvider(ReceiveCommand.Type.DELETE, "refs/tags/1.0.0", revision);
    
    assertThat(provider.getCreatedTags(), empty());
    assertTag("1.0.0", revision, provider.getDeletedTags());
  }
  
  /**
   * Tests {@link GitHookTagProvider} with a branch ref instead of a tag.
   */
  @Test
  public void testWithBranch(){
    String revision = "b2002b64013e54b78eac251df0672bd5d6a83aa7";
    GitHookTagProvider provider = createProvider(ReceiveCommand.Type.CREATE, "refs/heads/1.0.0", revision);
    
    assertThat(provider.getCreatedTags(), empty());
    assertThat(provider.getDeletedTags(), empty());
  }
  
  private void assertTag(String name, String revision, List<Tag> tags){
    assertNotNull(tags);
    assertFalse(tags.isEmpty());
    assertEquals(1, tags.size());
    Tag tag = tags.get(0);
    assertEquals(name, tag.getName());
    assertEquals(revision, tag.getRevision());
  }
  
  private GitHookTagProvider createProvider(ReceiveCommand.Type type, String ref, String id){
    OngoingStubbing<ObjectId> ongoing;
    if (type == ReceiveCommand.Type.CREATE){
      ongoing = when(command.getNewId());
    } else {
      ongoing = when(command.getOldId());
    }
    ongoing.thenReturn(ObjectId.fromString(id));
    
    when(command.getType()).thenReturn(type);
    when(command.getRefName()).thenReturn(ref);
    
    return new GitHookTagProvider(commands);
  }

}
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
    
package sonia.scm.repository.api;

import com.google.common.collect.Lists;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Tag;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GitHookTagProvider}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class GitHookTagProviderTest {

  private static final String ZERO = ObjectId.zeroId().getName();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

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
    GitHookTagProvider provider = createProvider(ReceiveCommand.Type.CREATE, "refs/tags/1.0.0", revision, ZERO);
    
    assertTag("1.0.0", revision, provider.getCreatedTags());
    assertThat(provider.getDeletedTags(), empty());
  }
  
  /**
   * Tests {@link GitHookTagProvider#getDeletedTags()}.
   */
  @Test
  public void testGetDeletedTags() {
    String revision = "b2002b64013e54b78eac251df0672bd5d6a83aa7";
    GitHookTagProvider provider = createProvider(ReceiveCommand.Type.DELETE, "refs/tags/1.0.0", ZERO, revision);
    
    assertThat(provider.getCreatedTags(), empty());
    assertTag("1.0.0", revision, provider.getDeletedTags());
  }
  
  /**
   * Tests {@link GitHookTagProvider} with a branch ref instead of a tag.
   */
  @Test
  public void testWithBranch(){
    String revision = "b2002b64013e54b78eac251df0672bd5d6a83aa7";
    GitHookTagProvider provider = createProvider(ReceiveCommand.Type.CREATE, "refs/heads/1.0.0", revision, revision);
    
    assertThat(provider.getCreatedTags(), empty());
    assertThat(provider.getDeletedTags(), empty());
  }

  /**
   * Tests {@link GitHookTagProvider} with update command.
   */
  @Test
  public void testUpdateTags() {
    String newId = "b2002b64013e54b78eac251df0672bd5d6a83aa7";
    String oldId = "e0f2be968b147ff7043684a7715d2fe852553db4";

    GitHookTagProvider provider = createProvider(ReceiveCommand.Type.UPDATE, "refs/tags/1.0.0", newId, oldId);
    assertTag("1.0.0", newId, provider.getCreatedTags());
    assertTag("1.0.0", oldId, provider.getDeletedTags());
  }

  private void assertTag(String name, String revision, List<Tag> tags){
    assertNotNull(tags);
    assertFalse(tags.isEmpty());
    assertEquals(1, tags.size());
    Tag tag = tags.get(0);
    assertEquals(name, tag.getName());
    assertEquals(revision, tag.getRevision());
  }
  
  private GitHookTagProvider createProvider(ReceiveCommand.Type type, String ref, String newId, String oldId){
    when(command.getNewId()).thenReturn(ObjectId.fromString(newId));
    when(command.getOldId()).thenReturn(ObjectId.fromString(oldId));
    when(command.getType()).thenReturn(type);
    when(command.getRefName()).thenReturn(ref);
    return new GitHookTagProvider(commands);
  }

}

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
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link GitHookBranchProvider}.
 *
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

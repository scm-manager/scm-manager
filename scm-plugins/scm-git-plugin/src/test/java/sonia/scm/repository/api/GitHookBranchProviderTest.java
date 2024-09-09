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

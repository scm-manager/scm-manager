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

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.junit.Test;
import sonia.scm.repository.spi.AbstractGitCommandTestBase;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitHookModificationsProviderTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldReturnModificationsForNormalUpdate() throws IOException {
    GitHookModificationsProvider provider = mockProviderWithChange(ReceiveCommand.Type.UPDATE);

    assertThat(provider.getModifications("rename"))
      .extracting("modifications")
      .asList()
      .hasSize(2);
  }

  @Test
  public void shouldReturnModificationsForFastForward() throws IOException {
    GitHookModificationsProvider provider = mockProviderWithChange(ReceiveCommand.Type.UPDATE_NONFASTFORWARD);

    assertThat(provider.getModifications("rename"))
      .extracting("modifications")
      .asList()
      .hasSize(2);
  }

  @Test
  public void shouldReturnEmptyModificationsForBranchWithRevertedCommit() throws IOException {
    GitHookModificationsProvider provider = mockProviderWithChange(ReceiveCommand.Type.UPDATE, "03ca33468c2094249973d0ca11b80243a20de368", "592d797cd36432e591416e8b2b98154f4f163411");

    assertThat(provider.getModifications("rename"))
      .extracting("modifications")
      .asList()
      .isEmpty();
  }

  @Test
  public void shouldReturnEmptyModificationsForDeletedBranch() throws IOException {
    GitHookModificationsProvider provider = mockProviderWithChange(
      ReceiveCommand.Type.DELETE,
      "0000000000000000000000000000000000000000",
      "fcd0ef1831e4002ac43ea539f4094334c79ea9ec");

    assertThat(provider.getModifications("rename"))
      .extracting("modifications")
      .asList()
      .hasSize(5)
      .extracting("path")
      .contains("a.txt", "b.txt", "c/d.txt", "c/e.txt", "f.txt");
  }

  @Test
  public void shouldReturnEmptyModificationsForCreatedBranch() throws IOException {
    GitHookModificationsProvider provider = mockProviderWithChange(
      ReceiveCommand.Type.CREATE,
      "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
      "0000000000000000000000000000000000000000");

    assertThat(provider.getModifications("rename"))
      .extracting("modifications")
      .asList()
      .hasSize(5)
      .extracting("path")
      .contains("a.txt", "b.txt", "c/d.txt", "c/e.txt", "f.txt");
  }

  private GitHookModificationsProvider mockProviderWithChange(ReceiveCommand.Type update) throws IOException {
    return mockProviderWithChange(
      update,
      "383b954b27e052db6880d57f1c860dc208795247",
      "fcd0ef1831e4002ac43ea539f4094334c79ea9ec");
  }

  private GitHookModificationsProvider mockProviderWithChange(ReceiveCommand.Type update, String newObjectId, String oldObjectId) throws IOException {
    ReceiveCommand receiveCommand = mock(ReceiveCommand.class);
    when(receiveCommand.getRefName()).thenReturn("refs/heads/rename");
    when(receiveCommand.getType()).thenReturn(update);
    when(receiveCommand.getNewId()).thenReturn(ObjectId.fromString(newObjectId));
    when(receiveCommand.getOldId()).thenReturn(ObjectId.fromString(oldObjectId));
    return new GitHookModificationsProvider(List.of(receiveCommand), createContext().open());
  }
}

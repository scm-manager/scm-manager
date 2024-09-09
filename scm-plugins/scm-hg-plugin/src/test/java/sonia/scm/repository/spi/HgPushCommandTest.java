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

package sonia.scm.repository.spi;

import org.javahg.Changeset;
import org.junit.Test;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.api.PushFailedException;
import sonia.scm.repository.api.PushResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HgPushCommandTest extends IncomingOutgoingTestBase {

  @Test
  public void testPush() throws IOException {
    writeNewFile(outgoing, outgoingDirectory, "a.txt", "content of a.txt");
    Changeset o1 = commit(outgoing, "added a");

    writeNewFile(outgoing, outgoingDirectory, "b.txt", "content of b.txt");
    Changeset o2 = commit(outgoing, "added b");

    HgPushCommand cmd = createPushCommand();
    PushCommandRequest request = new PushCommandRequest();
    request.setRemoteRepository(incomingRepository);

    PushResponse response = cmd.push(request);
    assertThat(response).isNotNull();
    assertThat(response.getChangesetCount()).isEqualTo(2);

    assertThat(incoming.changeset(o1.getNode())).isNotNull();
    assertThat(incoming.changeset(o2.getNode())).isNotNull();
  }

  @Test
  public void testForcePush() throws IOException {
    writeNewFile(outgoing, outgoingDirectory, "a.txt", "content of a.txt");
    Changeset outgoingCommit = commit(outgoing, "added a");

    writeNewFile(incoming, incomingDirectory, "a.txt", "conflicting change of a.txt");
    commit(incoming, "changed a");

    HgPushCommand cmd = createPushCommand();
    PushCommandRequest request = new PushCommandRequest();
    request.setRemoteRepository(incomingRepository);
    request.setForce(true);

    PushResponse response = cmd.push(request);
    assertThat(response).isNotNull();
    assertThat(response.getChangesetCount()).isEqualTo(1);

    assertThat(incoming.tip().getNode()).isEqualTo(outgoingCommit.getNode());
  }

  @Test
  public void testFailedPushBecauseOfConflict() throws IOException {
    writeNewFile(outgoing, outgoingDirectory, "a.txt", "content of a.txt");
    commit(outgoing, "added a");

    writeNewFile(incoming, incomingDirectory, "a.txt", "conflicting change of a.txt");
    Changeset incomingCommit = commit(incoming, "changed a");

    HgPushCommand cmd = createPushCommand();
    PushCommandRequest request = new PushCommandRequest();
    request.setRemoteRepository(incomingRepository);

    assertThatThrownBy(() -> cmd.push(request))
      .isInstanceOf(PushFailedException.class)
      .hasMessageContaining("Failed to push");

    assertThat(incoming.tip().getNode()).isEqualTo(incomingCommit.getNode());
  }

  private HgPushCommand createPushCommand() {
    HgConfigResolver resolver = new HgConfigResolver(handler);
    return new HgPushCommand(
      handler,
      new HgCommandContext(resolver, HgTestUtil.createFactory(handler, outgoingDirectory), outgoingRepository),
      new TemporaryConfigFactory(new GlobalProxyConfiguration(new ScmConfiguration()))
    );
  }
}

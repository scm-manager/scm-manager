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

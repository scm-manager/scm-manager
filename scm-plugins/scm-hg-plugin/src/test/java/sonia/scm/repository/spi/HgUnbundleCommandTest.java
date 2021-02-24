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

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.util.Archives;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HgUnbundleCommandTest extends AbstractHgCommandTestBase {

  private ScmEventBus eventBus;
  private HgUnbundleCommand unbundleCommand;
  private HgPostReceiveRepositoryHookEventFactory eventFactory;

  @Before
  public void initUnbundleCommand() {
    eventBus = mock(ScmEventBus.class);
    eventFactory = mock(HgPostReceiveRepositoryHookEventFactory.class);
    unbundleCommand = new HgUnbundleCommand(cmdContext, eventBus, new HgLazyChangesetResolver(HgTestUtil.createFactory(handler, repositoryDirectory), null), eventFactory);
  }

  @Test
  public void shouldUnbundleRepositoryFiles() throws IOException {
    when(eventFactory.createEvent(eq(cmdContext), any()))
      .thenReturn(new PostReceiveRepositoryHookEvent(new RepositoryHookEvent(null, repository, RepositoryHookType.POST_RECEIVE)));


    String filePath = "test-input";
    String fileContent = "HeartOfGold";
    UnbundleCommandRequest unbundleCommandRequest = createUnbundleCommandRequestForFile(filePath, fileContent);

    unbundleCommand.unbundle(unbundleCommandRequest);

    assertFileWithContentWasCreated(cmdContext.getDirectory(), filePath, fileContent);
    verify(eventBus).post(any(PostReceiveRepositoryHookEvent.class));
  }

  @Test
  public void shouldUnbundleNestedRepositoryFiles() throws IOException {
    String filePath = "objects/pack/test-input";
    String fileContent = "hitchhiker";
    UnbundleCommandRequest unbundleCommandRequest = createUnbundleCommandRequestForFile(filePath, fileContent);

    unbundleCommand.unbundle(unbundleCommandRequest);

    assertFileWithContentWasCreated(cmdContext.getDirectory(), filePath, fileContent);
  }

  private void assertFileWithContentWasCreated(File temp, String filePath, String fileContent) throws IOException {
    File createdFile = temp.toPath().resolve(filePath).toFile();
    assertThat(createdFile).exists();
    assertThat(Files.readLines(createdFile, StandardCharsets.UTF_8).get(0)).isEqualTo(fileContent);
  }

  private UnbundleCommandRequest createUnbundleCommandRequestForFile(String filePath, String fileContent) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TarArchiveOutputStream taos = Archives.createTarOutputStream(baos);
    addEntry(taos, filePath, fileContent);
    taos.finish();
    taos.close();

    ByteSource byteSource = ByteSource.wrap(baos.toByteArray());
    return new UnbundleCommandRequest(byteSource);
  }

  private void addEntry(TarArchiveOutputStream taos, String name, String input) throws IOException {
    TarArchiveEntry entry = new TarArchiveEntry(name);
    byte[] data = input.getBytes();
    entry.setSize(data.length);
    taos.putArchiveEntry(entry);
    taos.write(data);
    taos.closeArchiveEntry();
  }
}

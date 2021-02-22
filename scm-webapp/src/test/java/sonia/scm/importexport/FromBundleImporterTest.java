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

package sonia.scm.importexport;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnbundleCommandBuilder;
import sonia.scm.repository.api.UnbundleResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("UnstableApiUsage")
class FromBundleImporterTest {

  @Mock
  private RepositoryManager manager;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private ScmEventBus eventBus;

  @InjectMocks
  private FromBundleImporter importer;

  @Test
  void shouldImportCompressedBundle() throws IOException {
    URL dumpUrl = Resources.getResource("sonia/scm/api/v2/svn.dump.gz");
    byte[] svnDump = Resources.toByteArray(dumpUrl);

    UnbundleCommandBuilder ubc = mock(UnbundleCommandBuilder.class, RETURNS_SELF);
    when(ubc.unbundle(any(File.class))).thenReturn(new UnbundleResponse(42));
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(service.getUnbundleCommand()).thenReturn(ubc);
    InputStream in = new ByteArrayInputStream(svnDump);

    Consumer<Repository> repositoryConsumer = importer.unbundleImport(in, true);
    repositoryConsumer.accept(RepositoryTestData.createHeartOfGold("svn"));

    verify(ubc).setCompressed(true);
    verify(ubc).unbundle(any(File.class));
  }

  @Test
  void shouldImportNonCompressedBundle() throws IOException {
    URL dumpUrl = Resources.getResource("sonia/scm/api/v2/svn.dump");
    byte[] svnDump = Resources.toByteArray(dumpUrl);

    UnbundleCommandBuilder ubc = mock(UnbundleCommandBuilder.class, RETURNS_SELF);
    when(ubc.unbundle(any(File.class))).thenReturn(new UnbundleResponse(21));
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(service.getUnbundleCommand()).thenReturn(ubc);
    InputStream in = new ByteArrayInputStream(svnDump);

    Consumer<Repository> repositoryConsumer = importer.unbundleImport(in, false);
    repositoryConsumer.accept(RepositoryTestData.createHeartOfGold("svn"));

    verify(ubc, never()).setCompressed(true);
    verify(ubc).unbundle(any(File.class));
  }

}

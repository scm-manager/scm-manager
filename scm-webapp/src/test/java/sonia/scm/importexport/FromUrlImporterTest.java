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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FromUrlImporterTest {

  @Mock
  private RepositoryManager manager;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private ScmEventBus eventBus;

  @InjectMocks
  private FromUrlImporter importer;

  @BeforeEach
  void setUpMocks() {
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
  }

  @Test
  void shouldPullChangesFromRemoteUrl() throws IOException {
    PullCommandBuilder pullCommandBuilder = mock(PullCommandBuilder.class, RETURNS_SELF);
    when(service.getPullCommand()).thenReturn(pullCommandBuilder);

    Repository repository = RepositoryTestData.createHeartOfGold();
    FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
    parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");

    Consumer<Repository> repositoryConsumer = importer.pullChangesFromRemoteUrl(parameters);
    repositoryConsumer.accept(repository);

    verify(pullCommandBuilder).pull("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
  }

  @Test
  void shouldPullChangesFromRemoteUrlWithCredentials() {
    PullCommandBuilder pullCommandBuilder = mock(PullCommandBuilder.class, RETURNS_SELF);
    when(service.getPullCommand()).thenReturn(pullCommandBuilder);

    Repository repository = RepositoryTestData.createHeartOfGold();
    FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
    parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");
    parameters.setUsername("trillian");
    parameters.setPassword("secret");

    Consumer<Repository> repositoryConsumer = importer.pullChangesFromRemoteUrl(parameters);
    repositoryConsumer.accept(repository);

    verify(pullCommandBuilder).withUsername("trillian");
    verify(pullCommandBuilder).withPassword("secret");
  }

  @Test
  void shouldThrowImportFailedEvent() throws IOException {
    PullCommandBuilder pullCommandBuilder = mock(PullCommandBuilder.class, RETURNS_SELF);
    when(service.getPullCommand()).thenReturn(pullCommandBuilder);
    doThrow(ImportFailedException.class).when(pullCommandBuilder).pull(anyString());

    Repository repository = RepositoryTestData.createHeartOfGold();
    FromUrlImporter.RepositoryImportParameters parameters = new FromUrlImporter.RepositoryImportParameters();
    parameters.setImportUrl("https://scm-manager.org/scm/repo/scmadmin/scm-manager.git");

    Consumer<Repository> repositoryConsumer = importer.pullChangesFromRemoteUrl(parameters);
    assertThrows(ImportFailedException.class, () -> repositoryConsumer.accept(repository));
  }
}

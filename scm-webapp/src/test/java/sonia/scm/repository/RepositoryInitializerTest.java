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

package sonia.scm.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.Priority;
import sonia.scm.repository.api.ModifyCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryInitializerTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Mock
  private RepositoryServiceFactory repositoryServiceFactory;

  @Mock
  private RepositoryService repositoryService;

  @Mock(answer = Answers.RETURNS_SELF)
  private ModifyCommandBuilder modifyCommand;

  private final Repository repository = RepositoryTestData.createHeartOfGold("git");

  @BeforeEach
  void setUpModifyCommand() {
    when(repositoryServiceFactory.create(repository)).thenReturn(repositoryService);
    when(repositoryService.getModifyCommand()).thenReturn(modifyCommand);
  }

  @Test
  void shouldNotCommitIfInitializerDidNotMakeAnyChanges() {
    when(modifyCommand.isEmpty()).thenReturn(true);

    Set<RepositoryContentInitializer> repositoryContentInitializers = ImmutableSet.of(
      new NoOpInitializer()
    );

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, repositoryContentInitializers);
    initializer.initialize(repository, Collections.emptyMap());

    verify(modifyCommand, never()).setCommitMessage("initialize repository");
    verify(modifyCommand, never()).execute();

    verify(repositoryService).close();
  }

  @Test
  void shouldCallRepositoryContentInitializer() throws IOException {
    ModifyCommandBuilder.WithOverwriteFlagContentLoader readmeContentLoader = mockContentLoader("README.md");
    ModifyCommandBuilder.WithOverwriteFlagContentLoader licenseContentLoader = mockContentLoader("LICENSE.txt");

    Set<RepositoryContentInitializer> repositoryContentInitializers = ImmutableSet.of(
      new ReadmeContentInitializer(),
      new LicenseContentInitializer()
    );

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, repositoryContentInitializers);
    initializer.initialize(repository, Collections.emptyMap());

    verifyFileCreation(readmeContentLoader, "# HeartOfGold");
    verifyFileCreation(licenseContentLoader, "MIT");

    verify(modifyCommand, times(2)).setCommitMessage("initialize repository");
    verify(modifyCommand, times(2)).execute();

    verify(repositoryService).close();
  }

  @Test
  void shouldCallRepositoryContentInitializerWithInputStream() throws IOException {
    ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader = mockContentLoader("awesome.txt");

    Set<RepositoryContentInitializer> repositoryContentInitializers = ImmutableSet.of(
      new StreamingContentInitializer()
    );

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, repositoryContentInitializers);
    initializer.initialize(repository, Collections.emptyMap());

    verifyFileCreationWithStream(contentLoader, "awesome");

    verify(modifyCommand).setCommitMessage("initialize repository");
    verify(modifyCommand).execute();

    verify(repositoryService).close();
  }

  @Test
  void shouldRespectPriorityOrder() throws IOException {
    ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader = mock(ModifyCommandBuilder.WithOverwriteFlagContentLoader.class);
    when(contentLoader.setOverwrite(true)).thenReturn(contentLoader);

    when(modifyCommand.createFile(anyString())).thenReturn(contentLoader);

    AtomicReference<String> reference = new AtomicReference<>();
    when(contentLoader.withData(any(ByteSource.class))).thenAnswer(ic -> {
      ByteSource byteSource = ic.getArgument(0);
      reference.set(byteSource.asCharSource(StandardCharsets.UTF_8).read());
      return modifyCommand;
    });

    Set<RepositoryContentInitializer> repositoryContentInitializers = ImmutableSet.of(
      new LicenseContentInitializer(),
      new ReadmeContentInitializer()
    );

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, repositoryContentInitializers);
    initializer.initialize(repository, Collections.emptyMap());

    assertThat(reference.get()).isEqualTo("MIT");
  }

  @Test
  void shouldCloseRepositoryServiceOnException() throws IOException {
    ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader = mockContentLoader("README.md");
    doThrow(new IOException("epic fail")).when(contentLoader).withData(any(ByteSource.class));

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, ImmutableSet.of(new ReadmeContentInitializer()));
    Map<String, JsonNode> contextEntries = Collections.emptyMap();
    assertThrows(InternalRepositoryException.class, () -> initializer.initialize(repository, contextEntries));

    verify(repositoryService).close();
  }

  @Test
  void shouldCallRepositoryContentInitializerWithContext() throws IOException {
    ModifyCommandBuilder.WithOverwriteFlagContentLoader slartiContentLoader = mockContentLoader("Slarti.md");

    Set<RepositoryContentInitializer> repositoryContentInitializers = ImmutableSet.of(
      new NamedFileInitializer()
    );

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, repositoryContentInitializers);
    Named named = new Named();
    named.setName("Slarti");
    initializer.initialize(repository, Collections.singletonMap("named", mapper.valueToTree(named)));

    verifyFileCreation(slartiContentLoader, "# Named file");

    verify(modifyCommand).setCommitMessage("initialize repository");
    verify(modifyCommand).execute();

    verify(repositoryService).close();
  }

  @Test
  void shouldDoNoInitializationWithoutContextType() {
    Set<RepositoryContentInitializer> repositoryContentInitializers = ImmutableSet.of(
      new NamedFileInitializer()
    );

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, repositoryContentInitializers);
    initializer.initialize(repository, Collections.emptyMap());

    verify(modifyCommand, never()).createFile(any());
    verify(modifyCommand).setCommitMessage("initialize repository");
    verify(modifyCommand).execute();

    verify(repositoryService).close();
  }

  private ModifyCommandBuilder.WithOverwriteFlagContentLoader mockContentLoader(String path) {
    ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader = mock(ModifyCommandBuilder.WithOverwriteFlagContentLoader.class);
    doReturn(contentLoader).when(modifyCommand).createFile(path);
    when(contentLoader.setOverwrite(true)).thenReturn(contentLoader);
    return contentLoader;
  }

  private void verifyFileCreation(ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader, String expectedContent) throws IOException {
    ArgumentCaptor<ByteSource> captor = ArgumentCaptor.forClass(ByteSource.class);
    verify(contentLoader).withData(captor.capture());
    String content = captor.getValue().asCharSource(StandardCharsets.UTF_8).read();
    assertThat(content).isEqualTo(expectedContent);
  }

  private void verifyFileCreationWithStream(ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader, String expectedContent) throws IOException {
    ArgumentCaptor<InputStream> captor = ArgumentCaptor.forClass(InputStream.class);
    verify(contentLoader).withData(captor.capture());
    byte[] bytes = ByteStreams.toByteArray(captor.getValue());
    assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo(expectedContent);
  }

  private static class NamedFileInitializer implements RepositoryContentInitializer {

    @Override
    public void initialize(InitializerContext context) throws IOException {
      Optional<Named> named = context.getEntry("named", Named.class);
      if (named.isPresent()) {
        context.create(named.get().getName() + ".md").from("# Named file");
      }
    }
  }

  static class Named {
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @Priority(1)
  private static class ReadmeContentInitializer implements RepositoryContentInitializer {

    @Override
    public void initialize(InitializerContext context) throws IOException {
      Repository repository = context.getRepository();
      context.create("README.md").from("# " + repository.getName());
    }
  }

  @Priority(2)
  private static class LicenseContentInitializer implements RepositoryContentInitializer {

    @Override
    public void initialize(InitializerContext context) throws IOException {
      context.create("LICENSE.txt").from("MIT");
    }
  }

  @Priority(3)
  private static class NoOpInitializer implements RepositoryContentInitializer {

    @Override
    public void initialize(InitializerContext context) {
    }
  }

  private static class StreamingContentInitializer implements RepositoryContentInitializer {

    @Override
    public void initialize(InitializerContext context) throws IOException {
      context.create("awesome.txt").from(new ByteArrayInputStream("awesome".getBytes(StandardCharsets.UTF_8)));
    }
  }

}

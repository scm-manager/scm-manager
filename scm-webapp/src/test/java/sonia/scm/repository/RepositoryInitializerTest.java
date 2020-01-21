package sonia.scm.repository;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryInitializerTest {

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
  void shouldCallRepositoryContentInitializer() throws IOException {
    ModifyCommandBuilder.WithOverwriteFlagContentLoader readmeContentLoader = mockContentLoader("README.md");
    ModifyCommandBuilder.WithOverwriteFlagContentLoader licenseContentLoader = mockContentLoader("LICENSE.txt");

    Set<RepositoryContentInitializer> repositoryContentInitializers = ImmutableSet.of(
      new ReadmeContentInitializer(),
      new LicenseContentInitializer()
    );

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, repositoryContentInitializers);
    initializer.initialize(repository);

    verifyFileCreation(readmeContentLoader, "# HeartOfGold");
    verifyFileCreation(licenseContentLoader, "MIT");

    verify(modifyCommand).setCommitMessage("initialize repository");
    verify(modifyCommand).execute();

    verify(repositoryService).close();
  }

  @Test
  void shouldCallRepositoryContentInitializerWithInputStream() throws IOException {
    ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader = mockContentLoader("awesome.txt");

    Set<RepositoryContentInitializer> repositoryContentInitializers = ImmutableSet.of(
      new StreamingContentInitializer()
    );

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, repositoryContentInitializers);
    initializer.initialize(repository);

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
    initializer.initialize(repository);

    assertThat(reference.get()).isEqualTo("MIT");
  }

  @Test
  void shouldCloseRepositoryServiceOnException() throws IOException {
    ModifyCommandBuilder.WithOverwriteFlagContentLoader contentLoader = mockContentLoader("README.md");
    doThrow(new IOException("epic fail")).when(contentLoader).withData(any(ByteSource.class));

    RepositoryInitializer initializer = new RepositoryInitializer(repositoryServiceFactory, ImmutableSet.of(new ReadmeContentInitializer()));
    assertThrows(InternalRepositoryException.class, () -> initializer.initialize(repository));

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

  private static class StreamingContentInitializer implements RepositoryContentInitializer {

    @Override
    public void initialize(InitializerContext context) throws IOException {
      context.create("awesome.txt").from(new ByteArrayInputStream("awesome".getBytes(StandardCharsets.UTF_8)));
    }
  }

}

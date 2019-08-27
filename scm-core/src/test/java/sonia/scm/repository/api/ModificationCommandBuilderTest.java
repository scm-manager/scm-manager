package sonia.scm.repository.api;

import com.google.common.io.ByteSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import sonia.scm.repository.spi.ModificationCommand;
import sonia.scm.repository.util.WorkdirProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class ModificationCommandBuilderTest {

  @Mock
  ModificationCommand command;
  @Mock
  WorkdirProvider workdirProvider;

  ModificationCommandBuilder commandBuilder;

  @BeforeEach
  void initWorkdir(@TempDirectory.TempDir Path temp) throws IOException {
    lenient().when(workdirProvider.createNewWorkdir()).thenReturn(temp.toFile());
    commandBuilder = new ModificationCommandBuilder(command, workdirProvider);
  }

  @Test
  void shouldReturnTargetRevisionFromCommit() {
    when(command.commit()).thenReturn("target");

    String targetRevision = commandBuilder.execute();

    assertThat(targetRevision).isEqualTo("target");
  }

  @Test
  void shouldExecuteDelete() {
    commandBuilder
      .deleteFile("toBeDeleted")
      .execute();

    verify(command).delete("toBeDeleted");
  }

  @Test
  void shouldExecuteMove() {
    commandBuilder
      .moveFile("source", "target")
      .execute();

    verify(command).move("source", "target");
  }

  @Test
  void shouldExecuteCreateWithByteSourceContent() throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    List<String> contentCaptor = new ArrayList<>();
    doAnswer(new ExtractContent(contentCaptor)).when(command).create(nameCaptor.capture(), any());

    commandBuilder
      .createFile("toBeCreated").withData(ByteSource.wrap("content".getBytes()))
      .execute();

    assertThat(nameCaptor.getValue()).isEqualTo("toBeCreated");
    assertThat(contentCaptor).contains("content");
  }

  @Test
  void shouldExecuteCreateWithInputStreamContent() throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    List<String> contentCaptor = new ArrayList<>();
    doAnswer(new ExtractContent(contentCaptor)).when(command).create(nameCaptor.capture(), any());

    commandBuilder
      .createFile("toBeCreated").withData(new ByteArrayInputStream("content".getBytes()))
      .execute();

    assertThat(nameCaptor.getValue()).isEqualTo("toBeCreated");
    assertThat(contentCaptor).contains("content");
  }

  @Test
  void shouldExecuteCreateMultipleTimes() throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    List<String> contentCaptor = new ArrayList<>();
    doAnswer(new ExtractContent(contentCaptor)).when(command).create(nameCaptor.capture(), any());

    commandBuilder
      .createFile("toBeCreated_1").withData(new ByteArrayInputStream("content_1".getBytes()))
      .createFile("toBeCreated_2").withData(new ByteArrayInputStream("content_2".getBytes()))
      .execute();

    List<String> createdNames = nameCaptor.getAllValues();
    assertThat(createdNames.get(0)).isEqualTo("toBeCreated_1");
    assertThat(createdNames.get(1)).isEqualTo("toBeCreated_2");
    assertThat(contentCaptor).contains("content_1", "content_2");
  }

  @Test
  void shouldExecuteModify() throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    List<String> contentCaptor = new ArrayList<>();
    doAnswer(new ExtractContent(contentCaptor)).when(command).modify(nameCaptor.capture(), any());

    commandBuilder
      .modifyFile("toBeModified").withData(ByteSource.wrap("content".getBytes()))
      .execute();

    assertThat(nameCaptor.getValue()).isEqualTo("toBeModified");
    assertThat(contentCaptor).contains("content");
  }

  @Test
  void shouldDeleteTemporaryFiles(@TempDirectory.TempDir Path temp) throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
    doNothing().when(command).modify(nameCaptor.capture(), fileCaptor.capture());

    commandBuilder
      .modifyFile("toBeModified").withData(ByteSource.wrap("content".getBytes()))
      .execute();

    assertThat(Files.list(temp)).isEmpty();
  }

  private static class ExtractContent implements Answer {
    private final List<String> contentCaptor;

    public ExtractContent(List<String> contentCaptor) {
      this.contentCaptor = contentCaptor;
    }

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
      return contentCaptor.add(Files.readAllLines(((File) invocation.getArgument(1)).toPath()).get(0));
    }
  }
}

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
import sonia.scm.repository.Person;
import sonia.scm.repository.spi.ModifyCommand;
import sonia.scm.repository.spi.ModifyCommandRequest;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class ModifyCommandBuilderTest {

  @Mock
  ModifyCommand command;
  @Mock
  WorkdirProvider workdirProvider;
  @Mock
  ModifyCommand.Worker worker;

  ModifyCommandBuilder commandBuilder;
  Path workdir;

  @BeforeEach
  void initWorkdir(@TempDirectory.TempDir Path temp) throws IOException {
    workdir = Files.createDirectory(temp.resolve("workdir"));
    lenient().when(workdirProvider.createNewWorkdir()).thenReturn(workdir.toFile());
    commandBuilder = new ModifyCommandBuilder(command, workdirProvider);
  }

  @BeforeEach
  void initRequestCaptor() {
    when(command.execute(any())).thenAnswer(
      invocation -> {
        ModifyCommandRequest request = invocation.getArgument(0);
        for (ModifyCommandRequest.PartialRequest r : request.getRequests()) {
          r.execute(worker);
        }
        return "target";
      }
    );
  }

  @Test
  void shouldReturnTargetRevisionFromCommit() {
    String targetRevision = initCommand()
      .deleteFile("toBeDeleted")
      .execute();

    assertThat(targetRevision).isEqualTo("target");
  }

  @Test
  void shouldExecuteDelete() throws IOException {
    initCommand()
      .deleteFile("toBeDeleted")
      .execute();

    verify(worker).delete("toBeDeleted");
  }

  @Test
  void shouldExecuteCreateWithByteSourceContent() throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    List<String> contentCaptor = new ArrayList<>();
    doAnswer(new ExtractContent(contentCaptor)).when(worker).create(nameCaptor.capture(), any(), anyBoolean());

    initCommand()
      .createFile("toBeCreated").withData(ByteSource.wrap("content".getBytes()))
      .execute();

    assertThat(nameCaptor.getValue()).isEqualTo("toBeCreated");
    assertThat(contentCaptor).contains("content");
  }

  @Test
  void shouldExecuteCreateWithInputStreamContent() throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    List<String> contentCaptor = new ArrayList<>();
    doAnswer(new ExtractContent(contentCaptor)).when(worker).create(nameCaptor.capture(), any(), anyBoolean());

    initCommand()
      .createFile("toBeCreated").withData(new ByteArrayInputStream("content".getBytes()))
      .execute();

    assertThat(nameCaptor.getValue()).isEqualTo("toBeCreated");
    assertThat(contentCaptor).contains("content");
  }

  @Test
  void shouldExecuteCreateWithOverwriteFalseAsDefault() throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Boolean> overwriteCaptor = ArgumentCaptor.forClass(Boolean.class);
    List<String> contentCaptor = new ArrayList<>();
    doAnswer(new ExtractContent(contentCaptor)).when(worker).create(nameCaptor.capture(), any(), overwriteCaptor.capture());

    initCommand()
      .createFile("toBeCreated").withData(new ByteArrayInputStream("content".getBytes()))
      .execute();

    assertThat(nameCaptor.getValue()).isEqualTo("toBeCreated");
    assertThat(overwriteCaptor.getValue()).isFalse();
    assertThat(contentCaptor).contains("content");
  }

  @Test
  void shouldExecuteCreateWithOverwriteIfSet() throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Boolean> overwriteCaptor = ArgumentCaptor.forClass(Boolean.class);
    List<String> contentCaptor = new ArrayList<>();
    doAnswer(new ExtractContent(contentCaptor)).when(worker).create(nameCaptor.capture(), any(), overwriteCaptor.capture());

    initCommand()
      .createFile("toBeCreated").setOverwrite(true).withData(new ByteArrayInputStream("content".getBytes()))
      .execute();

    assertThat(nameCaptor.getValue()).isEqualTo("toBeCreated");
    assertThat(overwriteCaptor.getValue()).isTrue();
    assertThat(contentCaptor).contains("content");
  }

  @Test
  void shouldExecuteCreateMultipleTimes() throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    List<String> contentCaptor = new ArrayList<>();
    doAnswer(new ExtractContent(contentCaptor)).when(worker).create(nameCaptor.capture(), any(), anyBoolean());

    initCommand()
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
    doAnswer(new ExtractContent(contentCaptor)).when(worker).modify(nameCaptor.capture(), any());

    initCommand()
      .modifyFile("toBeModified").withData(ByteSource.wrap("content".getBytes()))
      .execute();

    assertThat(nameCaptor.getValue()).isEqualTo("toBeModified");
    assertThat(contentCaptor).contains("content");
  }

  private ModifyCommandBuilder initCommand() {
    return commandBuilder
      .setBranch("branch")
      .setCommitMessage("message")
      .setAuthor(new Person());
  }

  @Test
  void shouldDeleteTemporaryFiles(@TempDirectory.TempDir Path temp) throws IOException {
    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
    doNothing().when(worker).modify(nameCaptor.capture(), fileCaptor.capture());

    initCommand()
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

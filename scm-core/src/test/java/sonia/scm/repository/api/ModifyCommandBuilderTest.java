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

import com.google.common.io.ByteSource;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.spi.ModifyCommand;
import sonia.scm.repository.spi.ModifyCommandRequest;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.user.EMail;
import sonia.scm.user.User;

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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyCommandBuilderTest {

  private static final ScmConfiguration SCM_CONFIGURATION = new ScmConfiguration();

  @Mock
  ModifyCommand command;
  @Mock
  WorkdirProvider workdirProvider;
  @Mock
  ModifyCommand.Worker worker;

  ModifyCommandBuilder commandBuilder;
  Path workdir;

  @BeforeEach
  void initWorkdir(@TempDir Path temp) throws IOException {
    workdir = Files.createDirectory(temp.resolve("workdir"));
    lenient().when(workdirProvider.createNewWorkdir("1")).thenReturn(workdir.toFile());
    commandBuilder = new ModifyCommandBuilder(command, workdirProvider, "1", new EMail(SCM_CONFIGURATION));
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

  private ModifyCommandBuilder initCommand() {
    return commandBuilder
      .setBranch("branch")
      .setCommitMessage("message");
  }

  private void mockLoggedInUser(User loggedInUser) {
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
    PrincipalCollection principals = mock(PrincipalCollection.class);
    when(subject.getPrincipals()).thenReturn(principals);
    when(principals.oneByType(User.class)).thenReturn(loggedInUser);
  }

  @AfterEach
  void unbindSubjec() {
    ThreadContext.unbindSubject();
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

  @Nested
  class WithUserWithMail {

    @BeforeEach
    void initSubject() {
      User loggedInUser = new User("dent", "Arthur", "dent@hitchhiker.com");
      mockLoggedInUser(loggedInUser);
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

      verify(worker).delete("toBeDeleted", false);
    }

    @Test
    void shouldExecuteRecursiveDelete() throws IOException {
      initCommand()
        .deleteFile("toBeDeleted", true)
        .execute();

      verify(worker).delete("toBeDeleted", true);
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

    @Test
    void shouldDeleteTemporaryFiles(@TempDir Path temp) throws IOException {
      ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
      doNothing().when(worker).modify(nameCaptor.capture(), fileCaptor.capture());

      initCommand()
        .modifyFile("toBeModified").withData(ByteSource.wrap("content".getBytes()))
        .execute();

      assertThat(Files.list(temp)).isEmpty();
    }

    @Test
    void shouldUseMailFromUser() throws IOException {
      initCommand()
        .modifyFile("toBeModified").withData(ByteSource.wrap("content".getBytes()))
        .execute();

      verify(command).execute(argThat(modifyCommandRequest -> {
        assertThat(modifyCommandRequest.getAuthor().getMail()).isEqualTo("dent@hitchhiker.com");
        return true;
      }));
    }
  }

  @Nested
  class WithUserWithoutMail {

    @BeforeEach
    void initSubject() {
      User loggedInUser = new User("dent", "Arthur", null);
      mockLoggedInUser(loggedInUser);
    }

    @Test
    void shouldUseMailFromUser() throws IOException {
      SCM_CONFIGURATION.setMailDomainName("heart-of-gold.local");
      initCommand()
        .modifyFile("toBeModified").withData(ByteSource.wrap("content".getBytes()))
        .execute();

      verify(command).execute(argThat(modifyCommandRequest -> {
        assertThat(modifyCommandRequest.getAuthor().getMail()).isEqualTo("dent@heart-of-gold.local");
        return true;
      }));
    }
  }
}

package sonia.scm.repository.api;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import sonia.scm.repository.Person;
import sonia.scm.repository.spi.ModifyCommand;
import sonia.scm.repository.spi.ModifyCommandRequest;
import sonia.scm.repository.util.WorkdirProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class ModifyCommandBuilder {

  private final ModifyCommand command;
  private final File workdir;

  private final ModifyCommandRequest request = new ModifyCommandRequest();

  ModifyCommandBuilder(ModifyCommand command, WorkdirProvider workdirProvider) {
    this.command = command;
    this.workdir = workdirProvider.createNewWorkdir();
  }

  ModifyCommandBuilder setBranchToModify(String branchToModify) {
    return this;
  }

  ContentLoader createFile(String path) {
    return new ContentLoader(
      content -> request.addRequest(new ModifyCommandRequest.CreateFileRequest(path, content))
    );
  }

  ContentLoader modifyFile(String path) {
    return new ContentLoader(
      content -> request.addRequest(new ModifyCommandRequest.ModifyFileRequest(path, content))
    );
  }

  ModifyCommandBuilder deleteFile(String path) {
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest(path));
    return this;
  }

  ModifyCommandBuilder moveFile(String sourcePath, String targetPath) {
    request.addRequest(new ModifyCommandRequest.MoveFileRequest(sourcePath, targetPath));
    return this;
  }

  String execute() {
    Preconditions.checkArgument(request.isValid(), "commit message, author and branch are required");
    return command.execute(request);
  }

  private File loadData(ByteSource data) throws IOException {
    File file = createTemporaryFile();
    data.copyTo(Files.asByteSink(file));
    return file;
  }

  private File loadData(InputStream data) throws IOException {
    File file = createTemporaryFile();
    OutputStream out = Files.asByteSink(file).openBufferedStream();
    ByteStreams.copy(data, out);
    out.close();
    return file;
  }

  private File createTemporaryFile() throws IOException {
    return File.createTempFile("upload-", "", workdir);
  }

  public ModifyCommandBuilder setCommitMessage(String message) {
    request.setCommitMessage(message);
    return this;
  }

  public ModifyCommandBuilder setAuthor(Person author) {
    request.setAuthor(author);
    return this;
  }

  public ModifyCommandBuilder setBranch(String branch) {
    request.setBranch(branch);
    return this;
  }

  public class ContentLoader {

    private final Consumer<File> contentConsumer;

    private ContentLoader(Consumer<File> contentConsumer) {
      this.contentConsumer = contentConsumer;
    }

    public ModifyCommandBuilder withData(ByteSource data) throws IOException {
      File content = loadData(data);
      contentConsumer.accept(content);
      return ModifyCommandBuilder.this;
    }
    public ModifyCommandBuilder withData(InputStream data) throws IOException {
      File content = loadData(data);
      contentConsumer.accept(content);
      return ModifyCommandBuilder.this;
    }
  }
}

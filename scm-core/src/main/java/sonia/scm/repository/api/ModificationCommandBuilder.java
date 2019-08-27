package sonia.scm.repository.api;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import sonia.scm.repository.spi.ModificationCommand;
import sonia.scm.repository.util.WorkdirProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModificationCommandBuilder {

  private final ModificationCommand command;
  private final File workdir;

  private final List<FileModification> modifications = new ArrayList<>();

  ModificationCommandBuilder(ModificationCommand command, WorkdirProvider workdirProvider) {
    this.command = command;
    this.workdir = workdirProvider.createNewWorkdir();
  }

  ModificationCommandBuilder setBranchToModify(String branchToModify) {
    return this;
  }

  ContentLoader createFile(String path) {
    return new ContentLoader(
      content -> modifications.add(new CreateFile(path, content))
    );
  }

  ContentLoader modifyFile(String path) {
    return new ContentLoader(
      content -> modifications.add(new ModifyFile(path, content))
    );
  }

  ModificationCommandBuilder deleteFile(String path) {
    modifications.add(new DeleteFile(path));
    return this;
  }

  ModificationCommandBuilder moveFile(String sourcePath, String targetPath) {
    modifications.add(new MoveFile(sourcePath, targetPath));
    return this;
  }

  String execute() {
    modifications.forEach(FileModification::execute);
    modifications.forEach(FileModification::cleanup);
    return command.commit();
  }

  private Content loadData(ByteSource data) throws IOException {
    File file = createTemporaryFile();
    data.copyTo(Files.asByteSink(file));
    return new Content(file);
  }

  private Content loadData(InputStream data) throws IOException {
    File file = createTemporaryFile();
    OutputStream out = Files.asByteSink(file).openBufferedStream();
    ByteStreams.copy(data, out);
    out.close();
    return new Content(file);
  }

  private File createTemporaryFile() throws IOException {
    return File.createTempFile("upload-", "", workdir);
  }

  private interface FileModification {
    void execute();

    default void cleanup() {
    }
  }

  private class DeleteFile implements FileModification {
    private final String path;

    public DeleteFile(String path) {
      this.path = path;
    }

    @Override
    public void execute() {
      command.delete(path);
    }
  }

  private class MoveFile implements FileModification {
    private final String sourcePath;
    private final String targetPath;

    private MoveFile(String sourcePath, String targetPath) {
      this.sourcePath = sourcePath;
      this.targetPath = targetPath;
    }

    @Override
    public void execute() {
      command.move(sourcePath, targetPath);
    }
  }

  private abstract class DataBasedModification implements FileModification {

    private final Content content;

    DataBasedModification(Content content) {
      this.content = content;
    }

    public Content getContent() {
      return content;
    }
    @Override
    public void cleanup() {
      content.deleteFile();
    }
  }

  private class CreateFile extends DataBasedModification {

    private final String path;

    CreateFile(String path, Content content) {
      super(content);
      this.path = path;
    }
    @Override
    public void execute() {
      command.create(path, getContent().getFile());
    }
  }

  private class ModifyFile extends DataBasedModification {

    private final String path;

    ModifyFile(String path, Content content) {
      super(content);
      this.path = path;
    }
    @Override
    public void execute() {
      command.modify(path, getContent().getFile());
    }
  }

  public class ContentLoader {

    private final Consumer<Content> contentConsumer;

    private ContentLoader(Consumer<Content> contentConsumer) {
      this.contentConsumer = contentConsumer;
    }

    public ModificationCommandBuilder withData(ByteSource data) throws IOException {
      Content content = loadData(data);
      contentConsumer.accept(content);
      return ModificationCommandBuilder.this;
    }
    public ModificationCommandBuilder withData(InputStream data) throws IOException {
      Content content = loadData(data);
      contentConsumer.accept(content);
      return ModificationCommandBuilder.this;
    }
  }

  private class Content {

    private final File file;

    Content(File file) {
      this.file = file;
    }

    private File getFile() {
      return file;
    }
    public void deleteFile() {
      file.delete();
    }
  }
}

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

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Person;
import sonia.scm.repository.spi.ModifyCommand;
import sonia.scm.repository.spi.ModifyCommandRequest;
import sonia.scm.repository.util.AuthorUtil;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.user.EMail;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Use this {@link ModifyCommandBuilder} to make file changes to the head of a branch. You can
 * <ul>
 *   <li>create new files ({@link #createFile(String)} (with the option to overwrite a file, if it already exists; by
 *   default a {@link sonia.scm.AlreadyExistsException} will be thrown)</li>
 *   <li>modify existing files ({@link #modifyFile(String)}</li>
 *   <li>delete existing files ({@link #deleteFile(String)}</li>
 * </ul>
 *
 * You can collect multiple changes before they are executed with a call to {@link #execute()}.
 *
 * <p>Example:
 * <pre>
 * commandBuilder
 *     .setBranch("feature/branch")
 *     .setCommitMessage("make some changes")
 *     .setAuthor(new Person())
 *     .createFile("file/to/create").withData(inputStream)
 *     .deleteFile("old/file/to/delete")
 *     .execute();
 * </pre>
 */
public class ModifyCommandBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ModifyCommandBuilder.class);

  private final ModifyCommand command;
  private final File workdir;

  @Nullable
  private final EMail eMail;

  private final ModifyCommandRequest request = new ModifyCommandRequest();

  ModifyCommandBuilder(ModifyCommand command, WorkdirProvider workdirProvider, String repositoryId, @Nullable EMail eMail) {
    this.command = command;
    this.workdir = workdirProvider.createNewWorkdir(repositoryId);
    this.eMail = eMail;
  }

  /**
   * Create a new file. The content of the file will be specified in a subsequent call to
   * {@link ContentLoader#withData(ByteSource)} or {@link ContentLoader#withData(InputStream)}.
   * By default, an {@link sonia.scm.AlreadyExistsException} will be thrown, when there already
   * exists a file with the given path. You can disable this setting
   * {@link WithOverwriteFlagContentLoader#setOverwrite(boolean)} to <code>true</code>.
   * @param path The path and the name of the file that should be created.
   * @return The loader to specify the content of the new file.
   */
  public WithOverwriteFlagContentLoader createFile(String path) {
    return new WithOverwriteFlagContentLoader(
      (content, overwrite) -> request.addRequest(new ModifyCommandRequest.CreateFileRequest(path, content, overwrite))
    );
  }

  /**
   * Modify an existing file. The new content of the file will be specified in a subsequent call to
   * {@link ContentLoader#withData(ByteSource)} or {@link ContentLoader#withData(InputStream)}.
   * @param path The path and the name of the file that should be modified.
   * @return The loader to specify the new content of the file.
   */
  public SimpleContentLoader modifyFile(String path) {
    return new SimpleContentLoader(
      content -> request.addRequest(new ModifyCommandRequest.ModifyFileRequest(path, content))
    );
  }

  /**
   * Move existing directories or files.
   *
   * @param fromPath The source file or directory.
   * @return A sub builder to specify the target and further options for the move.
   * @since 2.28.0
   */
  public MoveBuilder move(String fromPath) {
    return new MoveBuilder(fromPath);
  }

  /**
   * Delete an existing file.
   * @param path The path and the name of the file that should be deleted.
   * @return This builder instance.
   */
  public ModifyCommandBuilder deleteFile(String path) {
    return this.deleteFile(path, false);
  }

  public ModifyCommandBuilder deleteFile(String path, boolean recursive) {
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest(path, recursive));
    return this;
  }

  /**
   * Apply the changes and create a new commit with the given message and author.
   * @return The revision of the new commit.
   */
  public String execute() {
    AuthorUtil.setAuthorIfNotAvailable(request, eMail);
    try {
      Preconditions.checkArgument(request.isValid(), "commit message and at least one request are required");
      return command.execute(request);
    } finally {
      try {
        IOUtil.delete(workdir);
      } catch (IOException e) {
        LOG.warn("could not delete temporary workdir '{}'", workdir, e);
      }
    }
  }

  public boolean isEmpty() {
    return request.isEmpty();
  }

  /**
   * Set the commit message for the new commit.
   * @return This builder instance.
   */
  public ModifyCommandBuilder setCommitMessage(String message) {
    request.setCommitMessage(message);
    return this;
  }

  /**
   * Set the author for the new commit.
   * @return This builder instance.
   */
  public ModifyCommandBuilder setAuthor(Person author) {
    request.setAuthor(author);
    return this;
  }

  /**
   * Set the branch the changes should be made upon.
   * @return This builder instance.
   */
  public ModifyCommandBuilder setBranch(String branch) {
    request.setBranch(branch);
    return this;
  }

  /**
   * Disables adding a verifiable signature to the modification commit.
   * @return This builder instance.
   * @since 2.4.0
   */
  public ModifyCommandBuilder disableSigning() {
    request.setSign(false);
    return this;
  }

  /**
   * Set the expected revision of the branch, before the changes are applied. If the branch does not have the
   * expected revision, a concurrent modification exception will be thrown when the command is executed and no
   * changes will be applied.
   * @return This builder instance.
   */
  public ModifyCommandBuilder setExpectedRevision(String expectedRevision) {
    request.setExpectedRevision(expectedRevision);
    return this;
  }

  public ModifyCommandBuilder useDefaultPath(boolean useDefaultPath) {
    request.setDefaultPath(useDefaultPath);
    return this;
  }

  public interface ContentLoader {
    /**
     * Specify the data of the file using a {@link ByteSource}.
     *
     * @return The builder instance.
     * @throws IOException If the data could not be read.
     */
    ModifyCommandBuilder withData(ByteSource data) throws IOException;

    /**
     * Specify the data of the file using an {@link InputStream}.
     * @return The builder instance.
     * @throws IOException If the data could not be read.
     */
    ModifyCommandBuilder withData(InputStream data) throws IOException;
  }

  public class SimpleContentLoader implements ContentLoader {

    private final Consumer<File> contentConsumer;

    private SimpleContentLoader(Consumer<File> contentConsumer) {
      this.contentConsumer = contentConsumer;
    }

    @Override
    public ModifyCommandBuilder withData(ByteSource data) throws IOException {
      File content = loadData(data);
      contentConsumer.accept(content);
      return ModifyCommandBuilder.this;
    }

    @Override
    public ModifyCommandBuilder withData(InputStream data) throws IOException {
      File content = loadData(data);
      contentConsumer.accept(content);
      return ModifyCommandBuilder.this;
    }
  }

  public class WithOverwriteFlagContentLoader implements ContentLoader {

    private final ContentLoader contentLoader;
    private boolean overwrite = false;

    private WithOverwriteFlagContentLoader(BiConsumer<File, Boolean> contentConsumer) {
      this.contentLoader = new SimpleContentLoader(file -> contentConsumer.accept(file, overwrite));
    }

    /**
     * Set this to <code>true</code> to overwrite the file if it already exists. Otherwise, an
     * {@link sonia.scm.AlreadyExistsException} will be thrown.
     * @return This loader instance.
     */
    public WithOverwriteFlagContentLoader setOverwrite(boolean overwrite) {
      this.overwrite = overwrite;
      return this;
    }

    @Override
    public ModifyCommandBuilder withData(ByteSource data) throws IOException {
      return contentLoader.withData(data);
    }

    @Override
    public ModifyCommandBuilder withData(InputStream data) throws IOException {
      return contentLoader.withData(data);
    }
  }

  public class MoveBuilder {

    private final String fromPath;
    private boolean overwrite = false;

    public MoveBuilder(String fromPath) {
      this.fromPath = fromPath;
    }

    /**
     * Set this to <code>true</code> to overwrite the path if it already exists. Otherwise an
     * {@link sonia.scm.AlreadyExistsException} will be thrown.
     * @return This move builder instance.
     */
    public MoveBuilder withOverwrite(boolean overwrite) {
      this.overwrite = overwrite;
      return this;
    }

    /**
     * Sets the target path the file or directory should be moved to.
     * @return The builder instance.
     */
    public ModifyCommandBuilder to(String toPath) {
      request.addRequest(new ModifyCommandRequest.MoveRequest(fromPath, toPath, overwrite));
      return ModifyCommandBuilder.this;
    }
  }

  private File loadData(ByteSource data) throws IOException {
    File file = createTemporaryFile();
    data.copyTo(Files.asByteSink(file));
    return file;
  }

  private File loadData(InputStream data) throws IOException {
    File file = createTemporaryFile();
    try (OutputStream out = Files.asByteSink(file).openBufferedStream()) {
      ByteStreams.copy(data, out);
    }
    return file;
  }

  private File createTemporaryFile() throws IOException {
    return File.createTempFile("upload-", "", workdir);
  }
}

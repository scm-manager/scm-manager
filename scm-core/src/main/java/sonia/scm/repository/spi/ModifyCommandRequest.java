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

package sonia.scm.repository.spi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Validateable;
import sonia.scm.repository.Person;
import sonia.scm.repository.util.AuthorUtil.CommandWithAuthor;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModifyCommandRequest implements Resetable, Validateable, CommandWithAuthor {

  private static final Logger LOG = LoggerFactory.getLogger(ModifyCommandRequest.class);

  private final List<PartialRequest> requests = new ArrayList<>();

  private Person author;
  private String commitMessage;
  private String branch;
  private String expectedRevision;
  private boolean defaultPath;
  private boolean sign = true;

  @Override
  public void reset() {
    requests.clear();
    author = null;
    commitMessage = null;
    branch = null;
    defaultPath = false;
    sign = true;
  }

  public void addRequest(PartialRequest request) {
    this.requests.add(request);
  }

  public boolean isEmpty() {
    return requests.isEmpty();
  }

  public void setAuthor(Person author) {
    this.author = author;
  }

  public void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public void setSign(boolean sign) {
    this.sign = sign;
  }

  public List<PartialRequest> getRequests() {
    return Collections.unmodifiableList(requests);
  }

  public Person getAuthor() {
    return author;
  }

  public String getCommitMessage() {
    return commitMessage;
  }

  public String getBranch() {
    return branch;
  }

  public String getExpectedRevision() {
    return expectedRevision;
  }

  public boolean isDefaultPath() {
    return defaultPath;
  }

  @Override
  public boolean isValid() {
    return StringUtils.isNotEmpty(commitMessage) && !requests.isEmpty();
  }

  public void setExpectedRevision(String expectedRevision) {
    this.expectedRevision = expectedRevision;
  }

  public void setDefaultPath(boolean defaultPath) {
    this.defaultPath = defaultPath;
  }

  public boolean isSign() {
    return sign;
  }

  public interface PartialRequest {
    void execute(ModifyCommand.Worker worker) throws IOException;
  }

  public static class DeleteFileRequest implements PartialRequest {
    private final String path;
    private final boolean recursive;

    /**
     * @deprecated This is kept for compatibility, only. Use {@link #DeleteFileRequest(String, boolean)} instead.
     */
    @Deprecated
    public DeleteFileRequest(String path) {
      this(path, false);
    }

    public DeleteFileRequest(String path, boolean recursive) {
      this.path = path;
      this.recursive = recursive;
    }

    @Override
    public void execute(ModifyCommand.Worker worker) throws IOException {
      worker.delete(path, recursive);
    }
  }

  private abstract static class ContentModificationRequest implements PartialRequest {

    private final File content;

    ContentModificationRequest(File content) {
      this.content = content;
    }

    File getContent() {
      return content;
    }

    void cleanup() {
      if (content.exists()) {
        try {
          IOUtil.delete(content);
        } catch (IOException e) {
          LOG.warn("could not delete temporary file {}", content, e);
        }
      }
    }
  }

  public static class CreateFileRequest extends ContentModificationRequest {

    private final String path;
    private final boolean overwrite;

    public CreateFileRequest(String path, File content, boolean overwrite) {
      super(content);
      this.path = path;
      this.overwrite = overwrite;
    }

    @Override
    public void execute(ModifyCommand.Worker worker) throws IOException {
      worker.create(path, getContent(), overwrite);
      cleanup();
    }
  }

  public static class ModifyFileRequest extends ContentModificationRequest {

    private final String path;

    public ModifyFileRequest(String path, File content) {
      super(content);
      this.path = path;
    }

    @Override
    public void execute(ModifyCommand.Worker worker) throws IOException {
      worker.modify(path, getContent());
      cleanup();
    }
  }

  /**
   * @since 2.28.0
   */
  public static class MoveRequest implements PartialRequest {

    private final String fromPath;
    private final boolean overwrite;
    private final String toPath;

    public MoveRequest(String fromPath, String toPath, boolean overwrite) {
      this.toPath = toPath;
      this.fromPath = fromPath;
      this.overwrite = overwrite;
    }

    @Override
    public void execute(ModifyCommand.Worker worker) throws IOException {
      worker.move(fromPath, toPath, overwrite);
    }
  }
}

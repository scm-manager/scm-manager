package sonia.scm.repository.spi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Validateable;
import sonia.scm.repository.Person;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModifyCommandRequest implements Resetable, Validateable {

  private static final Logger LOG = LoggerFactory.getLogger(ModifyCommandRequest.class);

  private final List<PartialRequest> requests = new ArrayList<>();

  private Person author;
  private String commitMessage;
  private String branch;
  private String expectedRevision;

  @Override
  public void reset() {
    requests.clear();
    author = null;
    commitMessage = null;
    branch = null;
  }

  public void addRequest(PartialRequest request) {
    this.requests.add(request);
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

  @Override
  public boolean isValid() {
    return StringUtils.isNotEmpty(commitMessage) && !requests.isEmpty();
  }

  public void setExpectedRevision(String expectedRevision) {
    this.expectedRevision = expectedRevision;
  }

  public interface PartialRequest {
    void execute(ModifyCommand.Worker worker) throws IOException;
  }

  public static class DeleteFileRequest implements PartialRequest {
    private final String path;

    public DeleteFileRequest(String path) {
      this.path = path;
    }

    @Override
    public void execute(ModifyCommand.Worker worker) throws IOException {
      worker.delete(path);
    }
  }

  public static class MoveFileRequest implements PartialRequest {
    private final String sourcePath;
    private final String targetPath;

    public MoveFileRequest(String sourcePath, String targetPath) {
      this.sourcePath = sourcePath;
      this.targetPath = targetPath;
    }

    @Override
    public void execute(ModifyCommand.Worker worker) {
      worker.move(sourcePath, targetPath);
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
      try {
        IOUtil.delete(content);
      } catch (IOException e) {
        LOG.warn("could not delete temporary file {}", content, e);
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
    public void execute(ModifyCommand.Worker worker) {
      worker.modify(path, getContent());
      cleanup();
    }
  }
}

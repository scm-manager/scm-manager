package sonia.scm.repository;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Singleton
public class GitHeadHandler implements GitHeadResolver, GitHeadModifier {

  private final GitRepositoryHandler repositoryHandler;

  @Inject
  public GitHeadHandler(GitRepositoryHandler repositoryHandler) {
    this.repositoryHandler = repositoryHandler;
  }

  @Override
  public String resolve(Repository repository) {
    File headFile = findHeadFile(repository);
    try {
      String line = Files.readFirstLine(headFile, Charsets.UTF_8);
      // TODO handle invalid head file
      int index = line.indexOf(GitUtil.REF_HEAD_PREFIX);
      return line.substring(index + GitUtil.REF_HEAD_PREFIX.length());
    } catch (IOException e) {
      // TODO
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void modify(Repository repository, String head) {
    File headFile = findHeadFile(repository);
    try {
      String line = "ref: " + GitUtil.REF_HEAD_PREFIX + head + "\n";
      Files.write(line, headFile, Charsets.UTF_8);
    } catch (IOException e) {
      // TODO
      throw Throwables.propagate(e);
    }
  }

  private File findHeadFile(Repository repository) {
    // TODO handle non bare repositories
    File directory = repositoryHandler.getDirectory(repository);
    return new File(directory, "HEAD");
  }
}

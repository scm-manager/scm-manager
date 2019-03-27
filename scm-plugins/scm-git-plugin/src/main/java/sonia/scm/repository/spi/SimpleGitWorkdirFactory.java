package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.util.SimpleWorkdirFactory;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SimpleGitWorkdirFactory extends SimpleWorkdirFactory<Repository, GitContext> implements GitWorkdirFactory {

  public SimpleGitWorkdirFactory() {
    super(null, new GitCloneProvider());
  }

  public SimpleGitWorkdirFactory(File poolDirectory) {
    super(poolDirectory, null, new GitCloneProvider());
  }

  private static class GitCloneProvider implements CloneProvider<Repository, GitContext> {

    @Override
    public Repository cloneRepository(GitContext context, File target) {
      try {
        return Git.cloneRepository()
          .setURI(createScmTransportProtocolUri(context.getDirectory()))
          .setDirectory(target)
          .call()
          .getRepository();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not clone working copy of repository", e);
      }
    }

    private String createScmTransportProtocolUri(File bareRepository) {
      return ScmTransportProtocol.NAME + "://" + bareRepository.getAbsolutePath();
    }
  }
}

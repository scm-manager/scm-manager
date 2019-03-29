package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.util.SimpleWorkdirFactory;

import java.io.File;

public class SimpleGitWorkdirFactory extends SimpleWorkdirFactory<Repository, GitContext> implements GitWorkdirFactory {

  public SimpleGitWorkdirFactory() {
  }

  SimpleGitWorkdirFactory(File poolDirectory) {
    super(poolDirectory);
  }

  @Override
  public ParentAndClone<Repository> cloneRepository(GitContext context, File target) {
    try {
      return new ParentAndClone<>(null, Git.cloneRepository()
        .setURI(createScmTransportProtocolUri(context.getDirectory()))
        .setDirectory(target)
        .call()
        .getRepository());
    } catch (GitAPIException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone working copy of repository", e);
    }
  }

  private String createScmTransportProtocolUri(File bareRepository) {
    return ScmTransportProtocol.NAME + "://" + bareRepository.getAbsolutePath();
  }

  @Override
  protected void closeRepository(Repository repository) {
    repository.close();
  }

  @Override
  protected sonia.scm.repository.Repository getScmRepository(GitContext context) {
    return context.getRepository();
  }
}

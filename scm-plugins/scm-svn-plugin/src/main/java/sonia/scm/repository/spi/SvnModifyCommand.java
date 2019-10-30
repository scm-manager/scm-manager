package sonia.scm.repository.spi;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnWorkDirFactory;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SvnModifyCommand implements ModifyCommand {

  private SvnContext context;
  private SvnWorkDirFactory workDirFactory;
  private Repository repository;

  public SvnModifyCommand(SvnContext context, Repository repository, SvnWorkDirFactory workDirFactory) {
    this.context = context;
    this.repository = repository;
    this.workDirFactory = workDirFactory;
  }

  @Override
  public String execute(ModifyCommandRequest request) {
    SVNClientManager clientManager = SVNClientManager.newInstance();
    try (WorkingCopy<File, File> workingCopy = workDirFactory.createWorkingCopy(context, null)) {
      File workingRepository = workingCopy.getWorkingRepository();
      for (ModifyCommandRequest.PartialRequest partialRequest : request.getRequests()) {
        try {
          SVNWCClient wcClient = clientManager.getWCClient();
          partialRequest.execute(new ModifyWorkerHelper() {
            @Override
            public void doScmDelete(String toBeDeleted){
              try {
                wcClient.doDelete(new File(String.format("%s/%s", workingRepository, toBeDeleted)), true, true, false);
              } catch (SVNException e) {
                throw new InternalRepositoryException(repository, "could not delete file from repository");
              }
            }

            @Override
            public void addFileToScm(String name, Path file) {
              try {
                wcClient.doAdd(file.toFile(), true, false, true, SVNDepth.INFINITY, false, true);
              } catch (SVNException e) {
                throw new InternalRepositoryException(repository, "could not add file to repository");
              }
            }

            @Override
            public File getWorkDir() {
              return workingRepository;
            }

            @Override
            public Repository getRepository() {
              return repository;
            }

            @Override
            public String getBranch() {
              return null;
            }
          });
        } catch (IOException e) {
          throw new InternalRepositoryException(repository, "could not read files from repository");
        }
      }
      try {
        SVNCommitInfo svnCommitInfo = clientManager.getCommitClient().doCommit(new File[]{workingRepository}, false,
          request.getCommitMessage(), null, null, false, true, SVNDepth.INFINITY);
        return String.valueOf(svnCommitInfo.getNewRevision());
      } catch (SVNException e) {
        throw new InternalRepositoryException(repository, "could not commit changes on repository");
      }
    }
  }
}

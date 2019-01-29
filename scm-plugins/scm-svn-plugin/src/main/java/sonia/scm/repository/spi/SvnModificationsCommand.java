package sonia.scm.repository.spi;

import lombok.extern.slf4j.Slf4j;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

import java.util.Collection;

@Slf4j
public class SvnModificationsCommand extends AbstractSvnCommand implements ModificationsCommand {

  SvnModificationsCommand(SvnContext context, Repository repository) {
    super(context, repository);
  }


  @Override
  @SuppressWarnings("unchecked")
  public Modifications getModifications(String revision) {
    final Modifications modifications = new Modifications();
    log.debug("get modifications {}", revision);
    try {
      if (SvnUtil.isTransactionEntryId(revision)) {

        SVNLookClient client = SVNClientManager.newInstance().getLookClient();
        client.doGetChanged(context.getDirectory(), SvnUtil.getTransactionId(revision),
          e -> SvnUtil.appendModification(modifications, e.getType(), e.getPath()), true);

        return modifications;

      } else {

        long revisionNumber = SvnUtil.getRevisionNumber(revision, repository);
        SVNRepository repo = open();
        Collection<SVNLogEntry> entries = repo.log(null, null, revisionNumber,
          revisionNumber, true, true);
        if (Util.isNotEmpty(entries)) {
          return SvnUtil.createModifications(entries.iterator().next(), revision);
        }
      }
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not open repository", ex);
    }
    return null;
  }

  @Override
  public Modifications getModifications(ModificationsCommandRequest request) {
    return getModifications(request.getRevision());
  }


}

package sonia.scm.repository.spi;

import lombok.extern.slf4j.Slf4j;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
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
    Modifications modifications = null;
    log.debug("get modifications {}", revision);
    try {
      long revisionNumber = SvnUtil.parseRevision(revision, repository);
      SVNRepository repo = open();
      Collection<SVNLogEntry> entries = repo.log(null, null, revisionNumber,
        revisionNumber, true, true);
      if (Util.isNotEmpty(entries)) {
        modifications = SvnUtil.createModifications(entries.iterator().next(), revision);
      }
    } catch (SVNException ex) {
      throw new InternalRepositoryException("could not open repository", ex);
    }
    return modifications;
  }

  @Override
  public Modifications getModifications(ModificationsCommandRequest request) {
    return getModifications(request.getRevision());
  }


}

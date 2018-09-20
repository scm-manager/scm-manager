package sonia.scm.repository.spi;

import sonia.scm.repository.Modifications;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;

import java.text.MessageFormat;

public class HgModificationsCommand extends AbstractCommand implements ModificationsCommand {

  HgModificationsCommand(HgCommandContext context, Repository repository) {
    super(context, repository);
  }


  @Override
  public Modifications getModifications(String revision) {
    com.aragost.javahg.Repository repository = open();
    HgLogChangesetCommand hgLogChangesetCommand = HgLogChangesetCommand.on(repository, getContext().getConfig());
    int hgRevision = hgLogChangesetCommand.rev(revision).singleRevision();
    Modifications modifications = hgLogChangesetCommand.rev(MessageFormat.format("{0}:{0}", hgRevision)).extractModifications();
    modifications.setRevision(revision);
    return modifications;
  }

  @Override
  public Modifications getModifications(ModificationsCommandRequest request) {
    return getModifications(request.getRevision());
  }


}

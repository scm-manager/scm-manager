package sonia.scm.repository.spi;

import org.apache.commons.lang.exception.CloneFailedException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnWorkDirFactory;
import sonia.scm.repository.util.SimpleWorkdirFactory;
import sonia.scm.repository.util.WorkdirProvider;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class SimpleSvnWorkDirFactory extends SimpleWorkdirFactory<File, File, SvnContext> implements SvnWorkDirFactory {

  @Inject
  public SimpleSvnWorkDirFactory(WorkdirProvider workdirProvider) {
    super(workdirProvider);
  }

  @Override
  protected Repository getScmRepository(SvnContext context) {
    return null;
  }

  @Override
  protected void closeRepository(File workingCopy) {
  }

  @Override
  protected void closeWorkdirInternal(File workdir) {
  }

  @Override
  protected ParentAndClone<File, File> cloneRepository(SvnContext context, File workingCopy, String initialBranch) throws IOException {

    final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();

    SVNURL source;
    try {
      source = SVNURL.fromFile(context.getDirectory());
    } catch (SVNException ex) {
      throw new CloneFailedException(ex.getMessage());
    }

    try {
      final SvnCheckout checkout = svnOperationFactory.createCheckout();
      checkout.setSingleTarget(SvnTarget.fromFile(workingCopy));
      checkout.setSource(SvnTarget.fromURL(source));
      checkout.run();
    } catch (SVNException ex) {
      throw new CloneFailedException(ex.getMessage());
    } finally {
      svnOperationFactory.dispose();
    }

    return new ParentAndClone<>(context.getDirectory(), workingCopy);
  }
}

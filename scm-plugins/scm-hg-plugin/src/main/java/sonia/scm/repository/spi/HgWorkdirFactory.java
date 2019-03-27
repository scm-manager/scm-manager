package sonia.scm.repository.spi;

import sonia.scm.repository.util.WorkdirFactory;

public interface HgWorkdirFactory extends WorkdirFactory<RepositoryCloseableWrapper, HgCommandContext> {
}

package sonia.scm.repository;

import org.eclipse.jgit.lib.Repository;
import sonia.scm.repository.spi.GitContext;
import sonia.scm.repository.util.WorkdirFactory;

public interface GitWorkdirFactory extends WorkdirFactory<Repository, Repository, GitContext> {
}

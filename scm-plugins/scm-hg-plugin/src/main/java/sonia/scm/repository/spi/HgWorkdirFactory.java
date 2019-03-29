package sonia.scm.repository.spi;

import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.PullCommand;
import sonia.scm.repository.util.WorkdirFactory;

public interface HgWorkdirFactory extends WorkdirFactory<Repository, HgCommandContext> {
  void configure(PullCommand pullCommand);
}

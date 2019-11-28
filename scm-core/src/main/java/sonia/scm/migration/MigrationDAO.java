package sonia.scm.migration;

import java.util.Collection;

public interface MigrationDAO {
  Collection<MigrationInfo> getAll();
}

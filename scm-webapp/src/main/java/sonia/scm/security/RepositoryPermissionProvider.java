package sonia.scm.security;

import com.google.inject.Inject;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleDAO;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List ;

public class RepositoryPermissionProvider {

  private final SystemRepositoryPermissionProvider systemRepositoryPermissionProvider;
  private final RepositoryRoleDAO repositoryRoleDAO;

  @Inject
  public RepositoryPermissionProvider(SystemRepositoryPermissionProvider systemRepositoryPermissionProvider, RepositoryRoleDAO repositoryRoleDAO) {
    this.systemRepositoryPermissionProvider = systemRepositoryPermissionProvider;
    this.repositoryRoleDAO = repositoryRoleDAO;
  }

  public Collection<String> availableVerbs() {
    return systemRepositoryPermissionProvider.availableVerbs();
  }

  public Collection<RepositoryRole> availableRoles() {
    List<RepositoryRole> availableSystemRoles = systemRepositoryPermissionProvider.availableRoles();
    List<RepositoryRole> customRoles = repositoryRoleDAO.getAll();

    return new AbstractList<RepositoryRole>() {
      @Override
      public RepositoryRole get(int index) {
        return index < availableSystemRoles.size()? availableSystemRoles.get(index): customRoles.get(index - availableSystemRoles.size());
      }

      @Override
      public int size() {
        return availableSystemRoles.size() + customRoles.size();
      }
    };
  }
}

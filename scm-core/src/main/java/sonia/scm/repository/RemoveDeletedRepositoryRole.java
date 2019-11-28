package sonia.scm.repository;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;

import java.util.Optional;

import static sonia.scm.HandlerEventType.DELETE;

@EagerSingleton
@Extension
public class RemoveDeletedRepositoryRole {

  private final RepositoryManager repositoryManager;

  @Inject
  public RemoveDeletedRepositoryRole(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  @Subscribe
  void handle(RepositoryRoleEvent event) {
    if (event.getEventType() == DELETE) {
      repositoryManager.getAll()
        .forEach(repository -> check(repository, event.getItem()));
    }
  }

  private void check(Repository repository, RepositoryRole role) {
    findPermission(repository, role)
      .ifPresent(permission -> removeFromPermissions(repository, permission));
  }

  private Optional<RepositoryPermission> findPermission(Repository repository, RepositoryRole item) {
    return repository.getPermissions()
      .stream()
      .filter(repositoryPermission -> item.getName().equals(repositoryPermission.getRole()))
      .findFirst();
  }

  private void removeFromPermissions(Repository repository, RepositoryPermission permission) {
    repository.removePermission(permission);
    repositoryManager.modify(repository);
  }
}

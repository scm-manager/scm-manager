package sonia.scm.repository;

import java.text.MessageFormat;

public class PermissionAlreadyExistsException extends RepositoryException {

  public PermissionAlreadyExistsException(Repository repository, String permissionName) {
    super(MessageFormat.format("the permission {0} of the repository {1}/{2} already exists", permissionName, repository.getNamespace(), repository.getName()));
  }

}

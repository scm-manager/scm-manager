package sonia.scm.repository;

import java.text.MessageFormat;

public class PermissionNotFoundException extends RepositoryException{


  public PermissionNotFoundException(Repository repository, String permissionName) {
    super(MessageFormat.format("the permission {0} of the repository {1}/{2} does not exists", permissionName,repository.getNamespace(), repository.getName() ));
  }

}

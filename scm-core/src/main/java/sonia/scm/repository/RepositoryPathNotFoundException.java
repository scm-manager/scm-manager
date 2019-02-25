
package sonia.scm.repository;

public class RepositoryPathNotFoundException extends Exception {

  public static final String REPOSITORY_PATH_NOT_FOUND = "Repository path not found";

  public RepositoryPathNotFoundException() {
    super(REPOSITORY_PATH_NOT_FOUND);
  }

}

package sonia.scm.repository;

import com.google.inject.servlet.RequestScoped;

/**
 * Holds an instance of {@link HgContext} in the request scope.
 */
@RequestScoped
public class HgContextRequestStore {

  private final HgContext context = new HgContext();

  public HgContext get() {
    return context;
  }

}

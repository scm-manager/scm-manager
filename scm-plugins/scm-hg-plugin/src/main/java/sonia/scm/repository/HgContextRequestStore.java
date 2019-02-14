package sonia.scm.repository;

import com.google.inject.servlet.RequestScoped;

/**
 * Holds an instance of {@link HgContext} in the request scope.
 *
 * <p>The problem seems to be that guice had multiple options for injecting HgContext. {@link HgContextProvider}
 * bound via Module and {@link HgContext} bound void {@link RequestScoped} annotation. It looks like that Guice 4
 * injects randomly the one or the other, in SCMv1 (Guice 3) everything works as expected.</p>
 *
 * <p>To fix the problem we have created this class annotated with {@link RequestScoped}, which holds an instance
 * of {@link HgContext}. This way only the {@link HgContextProvider} is used for injection.</p>
 */
@RequestScoped
public class HgContextRequestStore {

  private final HgContext context = new HgContext();

  public HgContext get() {
    return context;
  }

}

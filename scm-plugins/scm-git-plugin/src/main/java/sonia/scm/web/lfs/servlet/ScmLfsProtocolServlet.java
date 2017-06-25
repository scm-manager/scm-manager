package sonia.scm.web.lfs.servlet;

import org.eclipse.jgit.lfs.errors.LfsException;
import org.eclipse.jgit.lfs.server.LargeFileRepository;
import org.eclipse.jgit.lfs.server.LfsProtocolServlet;

/**
 * Provides an implementation for the git-lfs Batch API.
 *
 * @since 1.54
 * Created by omilke on 11.05.2017.
 */
public class ScmLfsProtocolServlet extends LfsProtocolServlet {

  private final LargeFileRepository repository;

  public ScmLfsProtocolServlet(LargeFileRepository largeFileRepository) {
    this.repository = largeFileRepository;
  }


  @Override
  protected LargeFileRepository getLargeFileRepository(LfsRequest request, String path) throws LfsException {
    return repository;
  }
}

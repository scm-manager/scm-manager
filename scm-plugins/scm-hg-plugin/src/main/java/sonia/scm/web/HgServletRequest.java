package sonia.scm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * {@link HttpServletRequestWrapper} which adds some functionality in order to support the mercurial client.
 */
public final class HgServletRequest extends HttpServletRequestWrapper {

  private HgServletInputStream hgServletInputStream;

  /**
   * Constructs a request object wrapping the given request.
   *
   * @param request
   * @throws IllegalArgumentException if the request is null
   */
  public HgServletRequest(HttpServletRequest request) {
    super(request);
  }

  @Override
  public HgServletInputStream getInputStream() throws IOException {
    if (hgServletInputStream == null) {
      hgServletInputStream = new HgServletInputStream(super.getInputStream());
    }
    return hgServletInputStream;
  }
}

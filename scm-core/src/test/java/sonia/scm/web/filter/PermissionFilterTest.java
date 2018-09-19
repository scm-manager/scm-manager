package sonia.scm.web.filter;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.ScmProviderHttpServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SubjectAware(configuration = "classpath:sonia/scm/shiro.ini")
public class PermissionFilterTest {

  public static final Repository REPOSITORY = new Repository("1", "git", "space", "name");

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  private final ScmProviderHttpServlet delegateServlet = mock(ScmProviderHttpServlet.class);

  private final PermissionFilter permissionFilter = new PermissionFilter(new ScmConfiguration(), delegateServlet) {
    @Override
    protected boolean isWriteRequest(HttpServletRequest request) {
      return writeRequest;
    }
  };

  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);

  private boolean writeRequest = false;

  @Test
  @SubjectAware(username = "reader", password = "secret")
  public void shouldPassForReaderOnReadRequest() throws IOException, ServletException {
    writeRequest = false;

    permissionFilter.service(request, response, REPOSITORY);

    verify(delegateServlet).service(request, response, REPOSITORY);
  }

  @Test
  @SubjectAware(username = "reader", password = "secret")
  public void shouldBlockForReaderOnWriteRequest() throws IOException, ServletException {
    writeRequest = true;

    permissionFilter.service(request, response, REPOSITORY);

    verify(response).sendError(eq(401), anyString());
    verify(delegateServlet, never()).service(request, response, REPOSITORY);
  }

  @Test
  @SubjectAware(username = "writer", password = "secret")
  public void shouldPassForWriterOnWriteRequest() throws IOException, ServletException {
    writeRequest = true;

    permissionFilter.service(request, response, REPOSITORY);

    verify(delegateServlet).service(request, response, REPOSITORY);
  }
}

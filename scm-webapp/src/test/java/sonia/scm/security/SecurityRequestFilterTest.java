package sonia.scm.security;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.authc.AuthenticationException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/shiro-001.ini")
public class SecurityRequestFilterTest {

  @Rule
  public ShiroRule shiroRule = new ShiroRule();

  @Mock
  private ResourceInfo resourceInfo;
  @Mock
  private ContainerRequestContext context;
  @InjectMocks
  private SecurityRequestFilter securityRequestFilter;

  @Test
  public void shouldAllowUnauthenticatedAccessForAnnotatedMethod() throws NoSuchMethodException {
    when(resourceInfo.getResourceMethod()).thenReturn(SecurityTestClass.class.getMethod("anonymousAccessAllowed"));

    securityRequestFilter.filter(context);
  }

  @Test(expected = AuthenticationException.class)
  public void shouldRejectUnauthenticatedAccessForAnnotatedMethod() throws NoSuchMethodException {
    when(resourceInfo.getResourceMethod()).thenReturn(SecurityTestClass.class.getMethod("loginRequired"));

    securityRequestFilter.filter(context);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldAllowAuthenticatedAccessForMethodWithoutAnnotation() throws NoSuchMethodException {
    when(resourceInfo.getResourceMethod()).thenReturn(SecurityTestClass.class.getMethod("loginRequired"));

    securityRequestFilter.filter(context);
  }

  private static class SecurityTestClass {
    @AllowAnonymousAccess
    public void anonymousAccessAllowed() {
    }

    public void loginRequired() {
    }
  }
}

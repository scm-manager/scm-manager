package sonia.scm.boot;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServletContextCleanerTest {

  @Mock
  private ServletContext servletContext;

  @Test
  public void testCleanup() {
    Set<String> names = ImmutableSet.of(
      "org.jboss.resteasy.Dispatcher",
      "resteasy.Deployment",
      "sonia.scm.Context",
      "org.eclipse.jetty.HttpServer",
      "javax.servlet.Context",
      "org.apache.shiro.SecurityManager"
    );

    when(servletContext.getAttributeNames()).thenReturn(toEnumeration(names));

    ServletContextCleaner.cleanup(servletContext);

    verify(servletContext).removeAttribute("org.jboss.resteasy.Dispatcher");
    verify(servletContext).removeAttribute("resteasy.Deployment");
    verify(servletContext).removeAttribute("sonia.scm.Context");
    verify(servletContext, never()).removeAttribute("org.eclipse.jetty.HttpServer");
    verify(servletContext, never()).removeAttribute("javax.servlet.Context");
    verify(servletContext).removeAttribute("org.apache.shiro.SecurityManager");
  }

  private <T> Enumeration<T> toEnumeration(Collection<T> collection) {
    return new Vector<>(collection).elements();
  }
}

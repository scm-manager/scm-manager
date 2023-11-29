/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.lifecycle;

import com.google.common.collect.ImmutableSet;
import jakarta.servlet.ServletContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
      "jakarta.servlet.Context",
      "org.apache.shiro.SecurityManager"
    );

    when(servletContext.getAttributeNames()).thenReturn(toEnumeration(names));

    ServletContextCleaner.cleanup(servletContext);

    verify(servletContext).removeAttribute("org.jboss.resteasy.Dispatcher");
    verify(servletContext).removeAttribute("resteasy.Deployment");
    verify(servletContext).removeAttribute("sonia.scm.Context");
    verify(servletContext, never()).removeAttribute("org.eclipse.jetty.HttpServer");
    verify(servletContext, never()).removeAttribute("jakarta.servlet.Context");
    verify(servletContext).removeAttribute("org.apache.shiro.SecurityManager");
  }

  private <T> Enumeration<T> toEnumeration(Collection<T> collection) {
    return new Vector<>(collection).elements();
  }
}

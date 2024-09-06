/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

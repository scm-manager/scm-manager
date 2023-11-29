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
    
package sonia.scm.legacy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.migration.MigrationDAO;
import sonia.scm.migration.MigrationInfo;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryLegacyProtocolRedirectFilterTest {

  @Mock
  MigrationDAO migrationDAO;
  @Mock
  RepositoryDAO repositoryDao;
  @Mock
  HttpServletRequest request;
  @Mock
  HttpServletResponse response;
  @Mock
  FilterChain filterChain;

  @BeforeEach
  void initRequest() {
    lenient().when(request.getContextPath()).thenReturn("/scm");
    lenient().when(request.getQueryString()).thenReturn("");
  }

  @Test
  void shouldNotRedirectForEmptyMigrationList() throws IOException, ServletException {
    when(request.getServletPath()).thenReturn("/git/old/name");

    new RepositoryLegacyProtocolRedirectFilter(migrationDAO, repositoryDao).doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldRedirectForExistingRepository() throws IOException, ServletException {
    when(migrationDAO.getAll()).thenReturn(singletonList(new MigrationInfo("id", "git", "old/name", "namespace", "name")));
    when(repositoryDao.get("id")).thenReturn(new Repository());
    when(request.getServletPath()).thenReturn("/git/old/name");

    new RepositoryLegacyProtocolRedirectFilter(migrationDAO, repositoryDao).doFilter(request, response, filterChain);

    verify(response).sendRedirect("/scm/repo/namespace/name");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void shouldRedirectForExistingRepositoryWithFurtherPathElements() throws IOException, ServletException {
    when(migrationDAO.getAll()).thenReturn(singletonList(new MigrationInfo("id", "git", "old/name", "namespace", "name")));
    when(repositoryDao.get("id")).thenReturn(new Repository());
    when(request.getServletPath()).thenReturn("/git/old/name/info/refs");

    new RepositoryLegacyProtocolRedirectFilter(migrationDAO, repositoryDao).doFilter(request, response, filterChain);

    verify(response).sendRedirect("/scm/repo/namespace/name/info/refs");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void shouldRedirectWithQueryParameters() throws IOException, ServletException {
    when(migrationDAO.getAll()).thenReturn(singletonList(new MigrationInfo("id", "git", "old/name", "namespace", "name")));
    when(repositoryDao.get("id")).thenReturn(new Repository());
    when(request.getServletPath()).thenReturn("/git/old/name/info/refs");
    when(request.getQueryString()).thenReturn("parameter=value");

    new RepositoryLegacyProtocolRedirectFilter(migrationDAO, repositoryDao).doFilter(request, response, filterChain);

    verify(response).sendRedirect("/scm/repo/namespace/name/info/refs?parameter=value");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void shouldNotRedirectWhenRepositoryHasBeenDeleted() throws IOException, ServletException {
    when(migrationDAO.getAll()).thenReturn(singletonList(new MigrationInfo("id", "git", "old/name", "namespace", "name")));
    when(repositoryDao.get("id")).thenReturn(null);
    when(request.getServletPath()).thenReturn("/git/old/name");

    new RepositoryLegacyProtocolRedirectFilter(migrationDAO, repositoryDao).doFilter(request, response, filterChain);

    verify(response, never()).sendRedirect(any());
    verify(filterChain).doFilter(request, response);
  }
}

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

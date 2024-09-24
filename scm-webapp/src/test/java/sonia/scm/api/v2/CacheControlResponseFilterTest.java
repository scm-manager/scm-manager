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

package sonia.scm.api.v2;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheControlResponseFilterTest {

  @Mock
  private ContainerRequestContext requestContext;

  @Mock
  private ContainerResponseContext responseContext;

  @Mock
  private MultivaluedMap<String, Object> headers;

  private final CacheControlResponseFilter filter = new CacheControlResponseFilter();

  @BeforeEach
  void setUpMocks() {
    when(responseContext.getHeaders()).thenReturn(headers);
  }

  @Test
  void shouldAddCacheControlHeader() {
    filter.filter(requestContext, responseContext);

    verify(headers).add("Cache-Control", "no-cache");
  }

  @Test
  void shouldNotSetHeaderIfLastModifiedIsNotNull() {
    when(responseContext.getLastModified()).thenReturn(new Date());

    filter.filter(requestContext, responseContext);

    verify(headers, never()).add("Cache-Control", "no-cache");
  }

  @Test
  void shouldNotSetHeaderIfEtagIsNotNull() {
    when(responseContext.getEntityTag()).thenReturn(new EntityTag("42"));

    filter.filter(requestContext, responseContext);

    verify(headers, never()).add("Cache-Control", "no-cache");
  }

  @Test
  void shouldNotOverrideExistingCacheControl() {
    when(headers.containsKey("Cache-Control")).thenReturn(true);

    filter.filter(requestContext, responseContext);

    verify(headers, never()).add("Cache-Control", "no-cache");
  }

}

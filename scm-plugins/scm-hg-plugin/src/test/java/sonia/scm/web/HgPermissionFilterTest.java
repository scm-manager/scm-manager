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

package sonia.scm.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgRepositoryHandler;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HgPermissionFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HgRepositoryHandler hgRepositoryHandler;

  @InjectMocks
  private HgPermissionFilter filter;

  @Before
  public void setUp() {
    when(hgRepositoryHandler.getConfig()).thenReturn(new HgGlobalConfig());
  }

  @Test
  public void testWrapRequestIfRequired() {
    assertSame(request, filter.wrapRequestIfRequired(request));

    HgGlobalConfig hgGlobalConfig = new HgGlobalConfig();
    hgGlobalConfig.setEnableHttpPostArgs(true);
    when(hgRepositoryHandler.getConfig()).thenReturn(hgGlobalConfig);

    assertThat(filter.wrapRequestIfRequired(request), is(instanceOf(HgServletRequest.class)));
  }
}

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

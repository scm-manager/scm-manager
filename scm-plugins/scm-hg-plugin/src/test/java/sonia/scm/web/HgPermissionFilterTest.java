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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryProvider;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static sonia.scm.web.WireProtocolRequestMockFactory.CMDS_HEADS_KNOWN_NODES;
import static sonia.scm.web.WireProtocolRequestMockFactory.Namespace.BOOKMARKS;
import static sonia.scm.web.WireProtocolRequestMockFactory.Namespace.PHASES;

/**
 * Unit tests for {@link HgPermissionFilter}.
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class HgPermissionFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private ScmConfiguration configuration;

  @Mock
  private RepositoryProvider repositoryProvider;

  @Mock
  private HgRepositoryHandler hgRepositoryHandler;

  private WireProtocolRequestMockFactory wireProtocol = new WireProtocolRequestMockFactory("/scm/hg/repo");

  @InjectMocks
  private HgPermissionFilter filter;

  @Before
  public void setUp() {
    when(hgRepositoryHandler.getConfig()).thenReturn(new HgGlobalConfig());
  }

  /**
   * Tests {@link HgPermissionFilter#wrapRequestIfRequired(HttpServletRequest)}.
   */
  @Test
  public void testWrapRequestIfRequired() {
    assertSame(request, filter.wrapRequestIfRequired(request));

    HgGlobalConfig hgGlobalConfig = new HgGlobalConfig();
    hgGlobalConfig.setEnableHttpPostArgs(true);
    when(hgRepositoryHandler.getConfig()).thenReturn(hgGlobalConfig);

    assertThat(filter.wrapRequestIfRequired(request), is(instanceOf(HgServletRequest.class)));
  }

  /**
   * Tests {@link HgPermissionFilter#isWriteRequest(HttpServletRequest)}.
   */
  @Test
  public void testIsWriteRequest() {
    // read methods
    assertFalse(isWriteRequest("GET"));
    assertFalse(isWriteRequest("HEAD"));
    assertFalse(isWriteRequest("TRACE"));
    assertFalse(isWriteRequest("OPTIONS"));

    // write methods
    assertTrue(isWriteRequest("POST"));
    assertTrue(isWriteRequest("PUT"));
    assertTrue(isWriteRequest("DELETE"));
    assertTrue(isWriteRequest("KA"));
  }

  /**
   * Tests {@link HgPermissionFilter#isWriteRequest(HttpServletRequest)} with enabled httppostargs option.
   */
  @Test
  public void testIsWriteRequestWithEnabledHttpPostArgs() {
    HgGlobalConfig config = new HgGlobalConfig();
    config.setEnableHttpPostArgs(true);
    when(hgRepositoryHandler.getConfig()).thenReturn(config);

    assertFalse(isWriteRequest("POST"));
    assertFalse(isWriteRequest("POST", "heads"));
    assertTrue(isWriteRequest("POST", "unbundle"));
  }

  private boolean isWriteRequest(String method) {
    return isWriteRequest(method, "capabilities");
  }

  private boolean isWriteRequest(String method, String command) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getQueryString()).thenReturn("cmd=" + command);
    when(request.getMethod()).thenReturn(method);
    return filter.isWriteRequest(request);
  }

  /**
   * Tests {@link HgPermissionFilter#isWriteRequest(HttpServletRequest)} with a set of requests, which are used for a
   * fresh clone of a repository.
   */
  @Test
  public void testIsWriteRequestWithClone() {
    assertIsReadRequest(wireProtocol.capabilities());
    assertIsReadRequest(wireProtocol.listkeys(BOOKMARKS));
    assertIsReadRequest(wireProtocol.batch(CMDS_HEADS_KNOWN_NODES));
    assertIsReadRequest(wireProtocol.listkeys(PHASES));
  }

  /**
   * Tests {@link HgPermissionFilter#isWriteRequest(HttpServletRequest)} with a set of requests, which are used for a
   * push of a single changeset.
   */
  @Test
  public void testIsWriteRequestWithSingleChangesetPush() {
    assertIsReadRequest(wireProtocol.capabilities());
    assertIsReadRequest(wireProtocol.batch(CMDS_HEADS_KNOWN_NODES.concat("c0ceccb3b2f0f5c977ff32b9337519e5f37942c2")));
    assertIsReadRequest(wireProtocol.listkeys(PHASES));
    assertIsReadRequest(wireProtocol.listkeys(BOOKMARKS));
    assertIsWriteRequest(wireProtocol.unbundle(261L, "686173686564+6768033e216468247bd031a0a2d9876d79818f8f"));
    assertIsReadRequest(wireProtocol.listkeys(PHASES));
    assertIsWriteRequest(wireProtocol.pushkey("c0ceccb3b2f0f5c977ff32b9337519e5f37942c2&namespace=phases&new=0&old=1"));
  }

  /**
   * Tests {@link HgPermissionFilter#isWriteRequest(HttpServletRequest)} with a set of requests, which are used for a
   * push to a single changeset.
   */
  @Test
  public void testIsWriteRequestWithMultipleChangesetsPush() {
    assertIsReadRequest(wireProtocol.capabilities());
    assertIsReadRequest(wireProtocol.batch(CMDS_HEADS_KNOWN_NODES.concat("ef5993bb4abb32a0565c347844c6d939fc4f4b98")));
    assertIsReadRequest(wireProtocol.listkeys(PHASES));
    assertIsReadRequest(wireProtocol.listkeys(BOOKMARKS));
    assertIsReadRequest(wireProtocol.branchmap());
    assertIsReadRequest(wireProtocol.listkeys(BOOKMARKS));
    assertIsWriteRequest(wireProtocol.unbundle(746L, "686173686564+95373ca7cd5371cb6c49bb755ee451d9ec585845"));
    assertIsReadRequest(wireProtocol.listkeys(PHASES));
    assertIsWriteRequest(wireProtocol.pushkey("ef5993bb4abb32a0565c347844c6d939fc4f4b98&namespace=phases&new=0&old=1"));
  }

  /**
   * Tests {@link HgPermissionFilter#isWriteRequest(HttpServletRequest)} with a set of requests, which are used for a
   * push of multiple branches to a new repository.
   */
  @Test
  public void testIsWriteRequestWithMutlipleBranchesToNewRepositoryPush() {
    assertIsReadRequest(wireProtocol.capabilities());
    assertIsReadRequest(wireProtocol.batch(CMDS_HEADS_KNOWN_NODES.concat("ef5993bb4abb32a0565c347844c6d939fc4f4b98")));
    assertIsReadRequest(wireProtocol.known("c0ceccb3b2f0f5c977ff32b9337519e5f37942c2+187ddf37e237c370514487a0bb1a226f11a780b3+b5914611f84eae14543684b2721eec88b0edac12+8b63a323606f10c86b30465570c2574eb7a3a989"));
    assertIsReadRequest(wireProtocol.listkeys(PHASES));
    assertIsReadRequest(wireProtocol.listkeys(BOOKMARKS));
    assertIsWriteRequest(wireProtocol.unbundle(913L, "686173686564+6768033e216468247bd031a0a2d9876d79818f8f"));
    assertIsReadRequest(wireProtocol.listkeys(PHASES));
    assertIsWriteRequest(wireProtocol.pushkey("ef5993bb4abb32a0565c347844c6d939fc4f4b98&namespace=phases&new=0&old=1"));
  }

  /**
   * Tests {@link HgPermissionFilter#isWriteRequest(HttpServletRequest)} with a set of requests, which are used for a
   * push of a bookmark.
   */
  @Test
  public void testIsWriteRequestWithBookmarkPush() {
    assertIsReadRequest(wireProtocol.capabilities());
    assertIsReadRequest(wireProtocol.batch(CMDS_HEADS_KNOWN_NODES.concat("ef5993bb4abb32a0565c347844c6d939fc4f4b98")));
    assertIsReadRequest(wireProtocol.listkeys(PHASES));
    assertIsReadRequest(wireProtocol.listkeys(BOOKMARKS));
    assertIsReadRequest(wireProtocol.listkeys(PHASES));
    assertIsWriteRequest(wireProtocol.pushkey("markone&namespace=bookmarks&new=ef5993bb4abb32a0565c347844c6d939fc4f4b98&old="));
  }

  /**
   * Tests {@link HgPermissionFilter#isWriteRequest(HttpServletRequest)} with a write request hidden in a batch GET
   * request.
   *
   * @see <a href="https://goo.gl/poascp">Issue #970</a>
   */
  @Test
  public void testIsWriteRequestWithBookmarkPushInABatch() {
    assertIsWriteRequest(wireProtocol.batch("pushkey key=markthree,namespace=bookmarks,new=187ddf37e237c370514487a0bb1a226f11a780b3,old="));
  }

  private void assertIsReadRequest(HttpServletRequest request) {
    assertFalse(filter.isWriteRequest(request));
  }

  private void assertIsWriteRequest(HttpServletRequest request) {
    assertTrue(filter.isWriteRequest(request));
  }
}

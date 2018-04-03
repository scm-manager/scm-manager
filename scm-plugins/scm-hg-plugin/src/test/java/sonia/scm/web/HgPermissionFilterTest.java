/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */

package sonia.scm.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryProvider;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
    when(hgRepositoryHandler.getConfig()).thenReturn(new HgConfig());
  }

  /**
   * Tests {@link HgPermissionFilter#wrapRequestIfRequired(HttpServletRequest)}.
   */
  @Test
  public void testWrapRequestIfRequired() {
    assertSame(request, filter.wrapRequestIfRequired(request));

    HgConfig hgConfig = new HgConfig();
    hgConfig.setEnableHttpPostArgs(true);
    when(hgRepositoryHandler.getConfig()).thenReturn(hgConfig);

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
    HgConfig config = new HgConfig();
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

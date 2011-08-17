/**
 * Copyright (c) 2010, Sebastian Sdorra
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



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import sonia.scm.config.ScmConfiguration;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public class HttpUtilTest
{

  /**
   * Method description
   *
   */
  @Test
  public void getCompleteUrlTest()
  {
    ScmConfiguration config = new ScmConfiguration();

    config.setBaseUrl("http://www.scm-manager.org/scm");
    assertEquals("http://www.scm-manager.org/scm/test/path",
                 HttpUtil.getCompleteUrl(config, "test/path"));
    assertEquals("http://www.scm-manager.org/scm/test/path",
                 HttpUtil.getCompleteUrl(config, "/test/path"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void getPortFromUrlTest()
  {
    assertTrue(HttpUtil.getPortFromUrl("http://www.scm-manager.org") == 80);
    assertTrue(HttpUtil.getPortFromUrl("https://www.scm-manager.org") == 443);
    assertTrue(HttpUtil.getPortFromUrl("http://www.scm-manager.org:8080")
               == 8080);
    assertTrue(
        HttpUtil.getPortFromUrl("http://www.scm-manager.org:8181/test/folder")
        == 8181);
  }

  /**
   * Method description
   *
   */
  @Test
  public void getServerPortTest()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getServerPort()).thenReturn(443);

    ScmConfiguration config = new ScmConfiguration();

    assertTrue(HttpUtil.getServerPort(config, request) == 443);
    config.setBaseUrl("http://www.scm-manager.org:8080");
    assertTrue(HttpUtil.getServerPort(config, request) == 8080);
  }

  /**
   * Method description
   *
   */
  @Test
  public void getStrippedURITest()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getRequestURI()).thenReturn("/scm/test/path");
    when(request.getContextPath()).thenReturn("/scm");
    assertEquals("/test/path",
                 HttpUtil.getStrippedURI(request, "/scm/test/path"));
    assertEquals("/test/path", HttpUtil.getStrippedURI(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void getUriWithoutEndSeperatorTest()
  {
    assertEquals("/test", HttpUtil.getUriWithoutEndSeperator("/test/"));
    assertEquals("/test/two", HttpUtil.getUriWithoutEndSeperator("/test/two/"));
    assertEquals("/test/two/three",
                 HttpUtil.getUriWithoutEndSeperator("/test/two/three"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void getUriWithoutStartSeperator()
  {
    assertEquals("test/", HttpUtil.getUriWithoutStartSeperator("/test/"));
    assertEquals("test/two/",
                 HttpUtil.getUriWithoutStartSeperator("/test/two/"));
    assertEquals("test/two/three",
                 HttpUtil.getUriWithoutStartSeperator("test/two/three"));
  }
}

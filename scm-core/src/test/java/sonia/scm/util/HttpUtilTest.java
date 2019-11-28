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

  @Test
  public void concatenateTest() {
    assertEquals(
      "/scm/git/hitchhiker/tricia",
      HttpUtil.concatenate("/scm", "git", "hitchhiker", "tricia")
    );
    assertEquals(
      "scm/git/hitchhiker/tricia",
      HttpUtil.concatenate("scm", "git", "hitchhiker", "tricia")
    );
  }

  /**
   * Method description
   *
   */
  @Test
  public void appendTest()
  {
    //J-
    assertEquals(
      "http://www.scm-manager/scm/test",
      HttpUtil.append("http://www.scm-manager/scm/", "test")
    );
    assertEquals(
      "http://www.scm-manager/scm/test",
      HttpUtil.append("http://www.scm-manager/scm", "test")
    );
    assertEquals(
      "http://www.scm-manager/scm/test",
      HttpUtil.append("http://www.scm-manager/scm", "/test")
    );
    assertEquals(
      "http://www.scm-manager/scm/test",
      HttpUtil.append("http://www.scm-manager/scm/", "/test")
    );
    //J+
  }

  /**
   * Method description
   *
   */
  @Test
  public void normalizeUrlTest()
  {
    assertEquals("http://www.scm-manager/scm",
      HttpUtil.normalizeUrl("http://www.scm-manager/scm"));
    assertEquals("http://www.scm-manager/scm",
      HttpUtil.normalizeUrl("http://www.scm-manager:80/scm"));
    assertEquals("https://www.scm-manager/scm",
      HttpUtil.normalizeUrl("https://www.scm-manager:443/scm"));
    assertEquals("https://www.scm-manager:8181/scm",
      HttpUtil.normalizeUrl("https://www.scm-manager:8181/scm"));
    assertEquals("http://www.scm-manager:8080/scm",
      HttpUtil.normalizeUrl("http://www.scm-manager:8080/scm"));
    assertEquals("http://www.scm-manager",
      HttpUtil.normalizeUrl("http://www.scm-manager:80"));
    assertEquals("https://www.scm-manager",
      HttpUtil.normalizeUrl("https://www.scm-manager:443"));
    assertEquals("http://www.scm-manager:8080",
      HttpUtil.normalizeUrl("http://www.scm-manager:8080"));
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure1()
  {
    HttpUtil.checkForCRLFInjection("any%0D%0A");
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure2()
  {
    HttpUtil.checkForCRLFInjection("123\nabc");
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure3()
  {
    HttpUtil.checkForCRLFInjection("123\rabc");
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure4()
  {
    HttpUtil.checkForCRLFInjection("123\r\nabc");
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure5()
  {
    HttpUtil.checkForCRLFInjection("123%abc");
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCheckForCRLFInjectionSuccess()
  {
    HttpUtil.checkForCRLFInjection("123");
    HttpUtil.checkForCRLFInjection("abc");
    HttpUtil.checkForCRLFInjection("abcka");
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateBaseUrl()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);
    String url = "https://www.scm-manager.org/test/as/db";

    when(request.getRequestURL()).thenReturn(new StringBuffer(url));
    when(request.getRequestURI()).thenReturn("/test/as/db");
    when(request.getContextPath()).thenReturn("/scm");
    assertEquals("https://www.scm-manager.org/scm",
      HttpUtil.createBaseUrl(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateForwardedUrl()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_HOST)).thenReturn(
      "www.scm-manager.org");
    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_PROTO)).thenReturn(
      "https");
    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_PORT)).thenReturn("443");
    when(request.getContextPath()).thenReturn("/scm");
    assertEquals("https://www.scm-manager.org:443/scm",
      HttpUtil.createForwardedBaseUrl(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateForwardedUrlWithPortAndProtoFromRequest()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_HOST)).thenReturn(
      "www.scm-manager.org");
    when(request.getScheme()).thenReturn("https");
    when(request.getServerPort()).thenReturn(443);
    when(request.getContextPath()).thenReturn("/scm");
    assertEquals("https://www.scm-manager.org:443/scm",
      HttpUtil.createForwardedBaseUrl(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateForwardedUrlWithPortInHost()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_HOST)).thenReturn(
      "www.scm-manager.org:443");
    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_PROTO)).thenReturn(
      "https");
    when(request.getContextPath()).thenReturn("/scm");
    assertEquals("https://www.scm-manager.org:443/scm",
      HttpUtil.createForwardedBaseUrl(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testCreateForwardedUrlWithPortInHostAndPortHeader()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_HOST)).thenReturn(
      "www.scm-manager.org:80");
    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_PROTO)).thenReturn(
      "https");
    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_PORT)).thenReturn("443");
    when(request.getContextPath()).thenReturn("/scm");
    assertEquals("https://www.scm-manager.org:443/scm",
      HttpUtil.createForwardedBaseUrl(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetCompleteUrl()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);
    String url = "https://www.scm-manager.org/test/as/db";

    when(request.getRequestURL()).thenReturn(new StringBuffer(url));
    when(request.getRequestURI()).thenReturn("/test/as/db");
    when(request.getScheme()).thenReturn("https");
    when(request.getServerPort()).thenReturn(443);
    when(request.getContextPath()).thenReturn("/scm");
    assertEquals("https://www.scm-manager.org/scm",
      HttpUtil.getCompleteUrl(request));
    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_HOST)).thenReturn(
      "scm.scm-manager.org");
    assertEquals("https://scm.scm-manager.org:443/scm",
      HttpUtil.getCompleteUrl(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testIsForwarded()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    assertFalse(HttpUtil.isForwarded(request));
    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_HOST)).thenReturn("");
    assertFalse(HttpUtil.isForwarded(request));
    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_HOST)).thenReturn("ser");
    assertTrue(HttpUtil.isForwarded(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testRemoveCRLFInjectionChars()
  {
    assertEquals("any0D0A", HttpUtil.removeCRLFInjectionChars("any%0D%0A"));
    assertEquals("123abc", HttpUtil.removeCRLFInjectionChars("123\nabc"));
    assertEquals("123abc", HttpUtil.removeCRLFInjectionChars("123\r\nabc"));
    assertEquals("123abc", HttpUtil.removeCRLFInjectionChars("123%abc"));
    assertEquals("123abc", HttpUtil.removeCRLFInjectionChars("123abc"));
    assertEquals("123", HttpUtil.removeCRLFInjectionChars("123"));

  }

  /**
   * Method description
   *
   */
  @Test
  public void userAgentStartsWithTest()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getHeader(HttpUtil.HEADER_USERAGENT)).thenReturn(
      "git/1.7.10.5997.gaa4aa");
    assertTrue(HttpUtil.userAgentStartsWith(request, "git/"));
    assertTrue(HttpUtil.userAgentStartsWith(request, "GIT/"));
    assertFalse(HttpUtil.userAgentStartsWith(request, "git/a"));
    assertFalse(HttpUtil.userAgentStartsWith(request, "sobbo/"));
  }

  //~--- get methods ----------------------------------------------------------

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

    config.setBaseUrl("http://www.scm-manager.org/scm/");
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

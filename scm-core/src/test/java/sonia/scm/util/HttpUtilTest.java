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

package sonia.scm.util;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import sonia.scm.config.ScmConfiguration;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class HttpUtilTest
{

  @Test
  public void testGetHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Test")).thenReturn("Value-One");

    String value = HttpUtil.getHeader(request, "Test", "Fallback");
    assertEquals("Value-One", value);
  }

  @Test
  public void testGetHeaderWithDefaultValue() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    String value = HttpUtil.getHeader(request, "Test", "Fallback");
    assertEquals("Fallback", value);
  }

  @Test
  public void testGetHeaderWithNullAsDefaultValue() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    String value = HttpUtil.getHeader(request, "Test", null);
    assertNull(value);
  }

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

   @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure1()
  {
    HttpUtil.checkForCRLFInjection("any%0D%0A");
  }

   @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure2()
  {
    HttpUtil.checkForCRLFInjection("123\nabc");
  }

   @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure3()
  {
    HttpUtil.checkForCRLFInjection("123\rabc");
  }

   @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure4()
  {
    HttpUtil.checkForCRLFInjection("123\r\nabc");
  }

   @Test(expected = IllegalArgumentException.class)
  public void testCheckForCRLFInjectionFailure5()
  {
    HttpUtil.checkForCRLFInjection("123%abc");
  }

   @Test
  public void testCheckForCRLFInjectionSuccess()
  {
    HttpUtil.checkForCRLFInjection("123");
    HttpUtil.checkForCRLFInjection("abc");
    HttpUtil.checkForCRLFInjection("abcka");
  }

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

  @Test(expected = IllegalStateException.class)
  public void shouldTrowIllegalStateExceptionWithoutForwardedHostHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpUtil.createForwardedBaseUrl(request);
  }

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

   @Test
  public void getPortFromUrlTest()
  {
    assertThat(HttpUtil.getPortFromUrl("http://www.scm-manager.org")).isEqualTo(80);
    assertThat(HttpUtil.getPortFromUrl("https://www.scm-manager.org")).isEqualTo(443);
    assertThat(HttpUtil.getPortFromUrl("http://www.scm-manager.org:8080")).isEqualTo(8080);
    assertThat(HttpUtil.getPortFromUrl("http://www.scm-manager.org:8181/test/folder")).isEqualTo(8181);
  }

   @Test
  public void getServerPortTest()
  {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getServerPort()).thenReturn(443);

    ScmConfiguration config = new ScmConfiguration();

    assertThat(HttpUtil.getServerPort(config, request)).isEqualTo(443);
    config.setBaseUrl("http://www.scm-manager.org:8080");
    assertThat(HttpUtil.getServerPort(config, request)).isEqualTo(8080);
  }

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

   @Test
  public void getUriWithoutEndSeperatorTest()
  {
    assertEquals("/test", HttpUtil.getUriWithoutEndSeperator("/test/"));
    assertEquals("/test/two", HttpUtil.getUriWithoutEndSeperator("/test/two/"));
    assertEquals("/test/two/three",
      HttpUtil.getUriWithoutEndSeperator("/test/two/three"));
  }

   @Test
  public void getUriWithoutStartSeperator()
  {
    assertEquals("test/", HttpUtil.getUriWithoutStartSeperator("/test/"));
    assertEquals("test/two/",
      HttpUtil.getUriWithoutStartSeperator("/test/two/"));
    assertEquals("test/two/three",
      HttpUtil.getUriWithoutStartSeperator("test/two/three"));
  }

  @Test
  public void testGetHeaderOrGetParameterWithHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Domain")).thenReturn("hitchhiker");

    assertThat(HttpUtil.getHeaderOrGetParameter(request, "Domain")).contains("hitchhiker");
  }

  @Test
  public void testGetHeaderOrGetParameterWithParameter() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getParameter("Domain")).thenReturn("hitchhiker");

    assertThat(HttpUtil.getHeaderOrGetParameter(request, "Domain")).contains("hitchhiker");
  }

  @Test
  public void testGetHeaderOrGetParameterOnPost() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn("POST");
    lenient().when(request.getParameter("Domain")).thenReturn("hitchhiker");

    assertThat(HttpUtil.getHeaderOrGetParameter(request, "Domain")).isEmpty();
  }

  @Test
  public void testIsWUIRequestWithHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader(HttpUtil.HEADER_SCM_CLIENT)).thenReturn(HttpUtil.SCM_CLIENT_WUI);

    assertThat(HttpUtil.isWUIRequest(request)).isTrue();
  }

  @Test
  public void testIsWUIRequestWithParameter() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getParameter(HttpUtil.HEADER_SCM_CLIENT)).thenReturn(HttpUtil.SCM_CLIENT_WUI);

    assertThat(HttpUtil.isWUIRequest(request)).isTrue();
  }

  @Test
  public void sendUnauthorized() throws IOException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpUtil.sendUnauthorized(response, "Hitchhikers finest");
    verify(response).setHeader(HttpUtil.HEADER_WWW_AUTHENTICATE, "Basic realm=\"Hitchhikers finest\"");
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, HttpUtil.STATUS_UNAUTHORIZED_MESSAGE);
  }

  @Test
  public void sendUnauthorizedWithDefaultRealmForNullDescription() throws IOException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpUtil.sendUnauthorized(response, null);
    verify(response).setHeader(HttpUtil.HEADER_WWW_AUTHENTICATE, "Basic realm=\"" + HttpUtil.AUTHENTICATION_REALM  + "\"");
  }

  @Test
  public void sendUnauthorizedWithDefaultRealmForEmptyDescription() throws IOException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpUtil.sendUnauthorized(response, "");
    verify(response).setHeader(HttpUtil.HEADER_WWW_AUTHENTICATE, "Basic realm=\"" + HttpUtil.AUTHENTICATION_REALM  + "\"");
  }
}

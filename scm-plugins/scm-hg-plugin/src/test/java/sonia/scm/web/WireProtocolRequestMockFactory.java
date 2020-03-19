/**
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

import com.google.common.collect.Lists;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WireProtocolRequestMockFactory {

  public enum Namespace {
    PHASES, BOOKMARKS;
  }

  public static final String CMDS_HEADS_KNOWN_NODES = "heads+%3Bknown+nodes%3D";

  private String repositoryPath;

  public WireProtocolRequestMockFactory(String repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  public HttpServletRequest capabilities() {
    return base("GET", "cmd=capabilities");
  }

  public HttpServletRequest listkeys(Namespace namespace) {
    HttpServletRequest request = base("GET", "cmd=capabilities");
    header(request, "vary", "X-HgArg-1");
    header(request, "x-hgarg-1", namespaceValue(namespace));
    return request;
  }

  public HttpServletRequest branchmap() {
    return base("GET", "cmd=branchmap");
  }

  public HttpServletRequest batch(String... args) {
    HttpServletRequest request = base("GET", "cmd=batch");
    args(request, "cmds", args);
    return request;
  }

  public HttpServletRequest unbundle(long contentLength, String... heads) {
    HttpServletRequest request = base("POST", "cmd=unbundle");
    header(request, "Content-Length", String.valueOf(contentLength));
    args(request, "heads", heads);
    return request;
  }

  public HttpServletRequest pushkey(String... keys) {
    HttpServletRequest request = base("POST", "cmd=pushkey");
    args(request, "key", keys);
    return request;
  }

  public HttpServletRequest known(String... nodes) {
    HttpServletRequest request = base("GET", "cmd=known");
    args(request, "nodes", nodes);
    return request;
  }

  private void args(HttpServletRequest request, String prefix, String[] values) {
    List<String> headers = Lists.newArrayList();

    StringBuilder vary = new StringBuilder();
    for ( int i=0; i<values.length; i++ ) {
      String header = "X-HgArg-" + (i+1);

      if (i>0) {
        vary.append(",");
      }

      vary.append(header);
      headers.add(header);

      header(request, header, prefix + "=" + values[i]);
    }
    header(request, "Vary", vary.toString());

    when(request.getHeaderNames()).thenReturn(Collections.enumeration(headers));
  }

  private HttpServletRequest base(String method, String queryStringValue) {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getMethod()).thenReturn(method);

    queryString(request, queryStringValue);

    header(request, "Accept", "application/mercurial-0.1");
    header(request, "Accept-Encoding", "identity");
    header(request, "User-Agent", "mercurial/proto-1.0 (Mercurial 4.3.1)");
    return request;
  }

  private void queryString(HttpServletRequest request, String queryString) {
    when(request.getQueryString()).thenReturn(queryString);
  }

  private void header(HttpServletRequest request, String header, String value) {
    when(request.getHeader(header)).thenReturn(value);
  }

  private String namespaceValue(Namespace namespace) {
    return "namespace=" + namespace.toString().toLowerCase(Locale.ENGLISH);
  }

}

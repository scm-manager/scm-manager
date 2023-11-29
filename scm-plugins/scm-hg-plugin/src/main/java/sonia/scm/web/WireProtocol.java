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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * WireProtocol provides methods for handling the mercurial wire protocol.
 *
 * @see <a href="https://goo.gl/WaVJzw">Mercurial Wire Protocol</a>
 */
public final class WireProtocol {

  private WireProtocol() {
  }

  @VisibleForTesting
  static List<String> commandsOf(HttpServletRequest request) {
    List<String> listOfCmds = Lists.newArrayList();

    String cmd = getCommandFromQueryString(request);
    if (cmd != null) {
      listOfCmds.add(cmd);
      if (isBatchCommand(cmd)) {
        parseHgArgHeaders(request, listOfCmds);
        handleHttpPostArgs(request, listOfCmds);
      }
    }
    return Collections.unmodifiableList(listOfCmds);
  }

  private static void handleHttpPostArgs(HttpServletRequest request, List<String> listOfCmds) {
    int hgArgsPostSize = request.getIntHeader("X-HgArgs-Post");
    if (hgArgsPostSize > 0) {

      if (request instanceof HgServletRequest) {
        HgServletRequest hgRequest = (HgServletRequest) request;

        parseHttpPostArgs(listOfCmds, hgArgsPostSize, hgRequest);
      } else {
        throw new IllegalArgumentException("could not process the httppostargs protocol without HgServletRequest");
      }

    }
  }

  private static void parseHttpPostArgs(List<String> listOfCmds, int hgArgsPostSize, HgServletRequest hgRequest) {
    try {
      byte[] bytes = hgRequest.getInputStream().readAndCapture(hgArgsPostSize);
      // we use iso-8859-1 for encoding, because the post args are normally http headers which are using iso-8859-1
      // see https://tools.ietf.org/html/rfc7230#section-3.2.4
      String hgArgs = new String(bytes, StandardCharsets.ISO_8859_1);
      String decoded = decodeValue(hgArgs);
      parseHgCommandHeader(listOfCmds, decoded);
    } catch (IOException ex) {
      throw Throwables.propagate(ex);
    }
  }

  private static void parseHgArgHeaders(HttpServletRequest request, List<String> listOfCmds) {
    Enumeration headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String header = (String) headerNames.nextElement();
      parseHgArgHeader(request, listOfCmds, header);
    }
  }

  private static void parseHgArgHeader(HttpServletRequest request, List<String> listOfCmds, String header) {
    if (isHgArgHeader(header)) {
      String value = getHeaderDecoded(request, header);
      parseHgArgValue(listOfCmds, value);
    }
  }

  private static void parseHgArgValue(List<String> listOfCmds, String value) {
    if (isHgArgCommandHeader(value)) {
      parseHgCommandHeader(listOfCmds, value);
    }
  }

  private static void parseHgCommandHeader(List<String> listOfCmds, String value) {
    String[] cmds = value.substring(5).split(";");
    for (String cmd : cmds ) {
      String normalizedCmd = normalize(cmd);
      int index = normalizedCmd.indexOf(' ');
      if (index > 0) {
        listOfCmds.add(normalizedCmd.substring(0, index));
      } else {
        listOfCmds.add(normalizedCmd);
      }
    }
  }

  private static String normalize(String cmd) {
    return cmd.trim().toLowerCase(Locale.ENGLISH);
  }

  private static boolean isHgArgCommandHeader(String value) {
    return value.startsWith("cmds=");
  }

  private static String getHeaderDecoded(HttpServletRequest request, String header) {
    return decodeValue(request.getHeader(header));
  }

  private static String decodeValue(String value) {
    return HttpUtil.decode(Strings.nullToEmpty(value));
  }

  private static boolean isHgArgHeader(String header) {
    return header.toLowerCase(Locale.ENGLISH).startsWith("x-hgarg-");
  }

  private static boolean isBatchCommand(String cmd) {
    return "batch".equalsIgnoreCase(cmd);
  }

  private static String getCommandFromQueryString(HttpServletRequest request) {
    // we can't use getParameter, because this would inspect the body for form parameters as well
    Multimap<String, String> queryParameterMap = createQueryParameterMap(request);

    Collection<String> cmd = queryParameterMap.get("cmd");
    Preconditions.checkArgument(cmd.size() <= 1, "found more than one cmd query parameter");
    Iterator<String> iterator = cmd.iterator();

    String command = null;
    if (iterator.hasNext()) {
      command = iterator.next();
    }
    return command;
  }

  private static Multimap<String,String> createQueryParameterMap(HttpServletRequest request) {
    Multimap<String,String> parameterMap = HashMultimap.create();

    String queryString = request.getQueryString();
    if (!Strings.isNullOrEmpty(queryString)) {

      String[] parameters = queryString.split("&");
      for (String parameter : parameters) {
        int index = parameter.indexOf('=');
        if (index > 0) {
          parameterMap.put(parameter.substring(0, index), parameter.substring(index + 1));
        } else {
          parameterMap.put(parameter, "true");
        }
      }

    }

    return parameterMap;
  }
}

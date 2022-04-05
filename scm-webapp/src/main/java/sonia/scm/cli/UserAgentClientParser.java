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

package sonia.scm.cli;

import com.google.common.base.Strings;
import org.jboss.resteasy.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public class UserAgentClientParser {

  private static final Logger LOG = LoggerFactory.getLogger(UserAgentClientParser.class);

  private final String userAgent;
  private int currentPosition = 0;

  private UserAgentClientParser(String userAgent) {
    this.userAgent = userAgent;
  }

  public static Client parse(String userAgent) {
    if (Strings.isNullOrEmpty(userAgent)) {
      return new Client("unknown", "unknown");
    }

    return new UserAgentClientParser(userAgent).parse();
  }

  private Client parse() {
    String name = parseName(userAgent);
    if (name.length() == userAgent.length()) {
      return new Client(name, "unknown");
    }
    String version = parseVersion(userAgent);
    if (name.equals("scm-cli")) {
      try {
        String os = parseOs(userAgent);
        String arch = parseArch(userAgent);
        String commitHash = parseCommitHash(userAgent);
        Instant buildTime = parseBuildTime(userAgent);
        return new Client(name, version, commitHash, buildTime, os, arch);
      }
      catch (IndexOutOfBoundsException | DateTimeParseException e) {
        LOG.debug("Could not parse user agent", e);
      }
    }
    return new Client(name, version);
  }

  private String parseCommitHash(String userAgent) {
    int start = userAgent.indexOf(" ", currentPosition);
    if (start < 0) {
      return null;
    }
    int semicolonAfterCommitHash = userAgent.indexOf(";", start);
    String commitHash = userAgent.substring(start + 1, semicolonAfterCommitHash);
    currentPosition = semicolonAfterCommitHash;
    return commitHash;
  }

  private String parseArch(String userAgent) {
    int end = userAgent.indexOf(";", currentPosition);
    if (end < 0) {
      end = userAgent.indexOf(")", currentPosition);
    }
    String arch = userAgent.substring(currentPosition + 1, end);
    currentPosition = end;
    return arch;
  }

  private String parseOs(String userAgent) {
    int firstParenthesis = userAgent.indexOf("(");
    int spaceAfterOs = userAgent.indexOf(" ", firstParenthesis);
    String os = userAgent.substring(firstParenthesis + 1, spaceAfterOs);
    currentPosition = spaceAfterOs;
    return os;
  }

  private String parseVersion(String userAgent) {
    int start = userAgent.indexOf(" ", currentPosition);
    if (start < 0) {
      return userAgent.substring(currentPosition + 1);
    }
    String version = userAgent.substring(currentPosition + 1, start);
    currentPosition = start;
    return version;
  }

  private String parseName(String userAgent) {
    int slash = userAgent.indexOf("/");
    if (slash < 0) {
      return userAgent;
    }
    String name = userAgent.substring(currentPosition, slash);
    currentPosition = slash;
    return name;
  }

  private Instant parseBuildTime(String userAgent) {
    int start = userAgent.indexOf(" ", currentPosition);
    if (start < 0) {
      return null;
    }
    int end = userAgent.indexOf(")", start);
    return Instant.parse(userAgent.substring(start + 1, end));
  }

}

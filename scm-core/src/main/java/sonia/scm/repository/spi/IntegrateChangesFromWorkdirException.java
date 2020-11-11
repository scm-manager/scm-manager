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

package sonia.scm.repository.spi;

import sonia.scm.ExceptionWithContext;
import sonia.scm.repository.Repository;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class IntegrateChangesFromWorkdirException extends ExceptionWithContext {

  static final String CODE_WITH_ADDITIONAL_MESSAGES = "CHRM7IQzo1";
  static final String CODE_WITHOUT_ADDITIONAL_MESSAGES = "ASSG1ehZ01";

  private static final Pattern SCM_MESSAGE_PATTERN = Pattern.compile(".*\\[SCM\\] (.*)");

  public static MessageExtractor withPattern(Pattern pattern) {
    return new MessageExtractor(pattern);
  }

  public static IntegrateChangesFromWorkdirException forMessage(Repository repository, String message) {
    return new MessageExtractor(SCM_MESSAGE_PATTERN).forMessage(repository, message);
  }

  private IntegrateChangesFromWorkdirException(Repository repository, List<AdditionalMessage> additionalMessages) {
    super(entity(repository).build(), additionalMessages, "errors from hook");
  }

  @Override
  public String getCode() {
    return getAdditionalMessages().isEmpty()? CODE_WITHOUT_ADDITIONAL_MESSAGES : CODE_WITH_ADDITIONAL_MESSAGES;
  }

  public static class MessageExtractor {

    private final Pattern extractorPattern;

    public MessageExtractor(Pattern extractorPattern) {
      this.extractorPattern = extractorPattern;
    }

    public IntegrateChangesFromWorkdirException forMessage(Repository repository, String message) {
      return new IntegrateChangesFromWorkdirException(repository, stream(message.split("\\n"))
        .map(extractorPattern::matcher)
        .filter(Matcher::matches)
        .map(matcher -> new AdditionalMessage(null, matcher.group(1)))
        .collect(Collectors.toList()));
    }
  }
}

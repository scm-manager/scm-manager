/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

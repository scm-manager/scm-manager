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

package sonia.scm.it.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.regex.Pattern;

public class RegExMatcher extends BaseMatcher<String> {
  public static Matcher<String> matchesPattern(String pattern) {
    return new RegExMatcher(pattern);
  }

  private final String pattern;

  private RegExMatcher(String pattern) {
    this.pattern = pattern;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("matching to regex pattern \"" + pattern + "\"");
  }

  @Override
  public boolean matches(Object o) {
    return o != null && Pattern.compile(pattern).matcher(o.toString()).matches();
  }
}

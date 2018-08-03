package sonia.scm.it;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.regex.Pattern;

class RegExMatcher extends BaseMatcher<String> {
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
    return Pattern.compile(pattern).matcher(o.toString()).matches();
  }
}

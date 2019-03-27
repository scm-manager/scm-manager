package sonia.scm.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static sonia.scm.util.ValidationUtil.REGEX_NAME;

@RunWith(Parameterized.class)
public class ValidationUtil_IllegalCharactersTest {

  private static final List<Character> ACCEPTED_CHARS = asList('@', '_', '-', '.');

  private final Pattern userGroupPattern=Pattern.compile(REGEX_NAME);

  private final String expression;

  public ValidationUtil_IllegalCharactersTest(String expression) {
    this.expression = expression;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<String[]> createParameters() {
    return Stream.concat(IntStream.range(0x20, 0x2f).mapToObj(i -> (char) i),    // chars before '0'
           Stream.concat(IntStream.range(0x3a, 0x40).mapToObj(i -> (char) i),    // chars between '9' and 'A'
           Stream.concat(IntStream.range(0x5b, 0x60).mapToObj(i -> (char) i),    // chars between 'Z' and 'a'
                         IntStream.range(0x7b, 0xff).mapToObj(i -> (char) i))))  // chars after 'z'
      .filter(c -> !ACCEPTED_CHARS.contains(c))
      .flatMap(c -> Stream.of("abc" + c + "xyz", "@" + c, c + "tail"))
      .map(c -> new String[] {c})
      .collect(Collectors.toList());
  }

  @Test
  public void shouldNotAcceptSpecialCharacters() {
    assertFalse(userGroupPattern.matcher(expression).matches());
  }
}

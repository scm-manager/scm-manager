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

import org.junit.jupiter.api.Test;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JexlUrlParserTest {

  @Test
  void shouldParseUrlWithoutExpression() {
    JexlUrlParser jexlUrlParser = new JexlUrlParser();
    Map<String, Object> env = new HashMap<>();
    env.put("changeset", new Changeset("1", 1L, Person.toPerson("trillian")));

    String parsedUrl = jexlUrlParser.parse("http://hitchhiker.org").evaluate(env);

    assertThat(parsedUrl).isEqualTo("http://hitchhiker.org");
  }

  @Test
  void shouldParseUrlWithExpression() {
    JexlUrlParser jexlUrlParser = new JexlUrlParser();
    Map<String, Object> env = new HashMap<>();
    env.put("changeset", new Changeset("1", 1L, Person.toPerson("trillian")));

    String parsedUrl = jexlUrlParser.parse("http://${changeset.author.name}.org").evaluate(env);

    assertThat(parsedUrl).isEqualTo("http://trillian.org");
  }
}

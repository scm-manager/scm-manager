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

import { Hit } from "@scm-manager/ui-types";

export const javaHit: Hit = {
  score: 2.5,
  fields: {
    content: {
      highlighted: true,
      fragments: [
        "import org.slf4j.LoggerFactory;\n\nimport java.util.Date;\nimport java.util.HashMap;\nimport java.util.Map;\nimport java.util.concurrent.TimeUnit;\n\n/**\n * Jwt implementation of {@link <|[[--AccessTokenBuilder--]]|>}.\n * \n * @author Sebastian Sdorra\n * @since 2.0.0\n */\npublic final class <|[[--JwtAccessTokenBuilder--]]|> implements <|[[--AccessTokenBuilder--]]|> {\n\n  /**\n   * the logger for <|[[--JwtAccessTokenBuilder--]]|>\n   */\n  private static final Logger LOG = LoggerFactory.getLogger(<|[[--JwtAccessTokenBuilder.class--]]|>);\n  \n  private final KeyGenerator keyGenerator; \n  private final SecureKeyResolver keyResolver; \n  \n  private String subject;\n  private String issuer;\n",
        "  private final Map<String,Object> custom = Maps.newHashMap();\n  \n  <|[[--JwtAccessTokenBuilder--]]|>(KeyGenerator keyGenerator, SecureKeyResolver keyResolver)  {\n    this.keyGenerator = keyGenerator;\n    this.keyResolver = keyResolver;\n  }\n\n  @Override\n  public <|[[--JwtAccessTokenBuilder--]]|> subject(String subject) {\n",
        '  public <|[[--JwtAccessTokenBuilder--]]|> custom(String key, Object value) {\n    Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "null or empty value not allowed");\n    Preconditions.checkArgument(value != null, "null or empty value not allowed");\n'
      ]
    }
  },
  _links: {}
};

export const bashHit: Hit = {
  score: 2.5,
  fields: {
    content: {
      highlighted: true,
      fragments: [
        '# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n# SOFTWARE.\n#\n\n<|[[--getent--]]|> group scm >/dev/null || groupadd -r scm\n<|[[--getent--]]|> passwd scm >/dev/null || \\\n    useradd -r -g scm -M \\\n    -s /sbin/nologin -d /var/lib/scm \\\n    -c "user for the scm-server process" scm\nexit 0\n\n'
      ]
    }
  },
  _links: {}
};

export const markdownHit: Hit = {
  score: 2.5,
  fields: {
    content: {
      highlighted: true,
      fragments: [
        "---\ntitle: SCM-Manager v2 Test <|[[--Cases--]]|>\n---\n\nDescribes the expected behaviour for SCMM v2 REST Resources using manual tests.\n\nThe following states general test <|[[--cases--]]|> per HTTP Method and en expected return code as well as exemplary curl calls.\nResource-specifics are stated \n\n## Test <|[[--Cases--]]|>\n\n### GET\n\n- Collection Resource (e.g. `/users`)\n    - Without parameters -> 200\n    - Parameters\n        - `?pageSize=1` -> Only one embedded element, pageTotal reflects the correct number of pages, `last` link points to last page.\n        - `?pageSize=1&page=1` -> `next` link points to page 0 ; `prev` link points to page 2\n        - `?sortBy=admin` -> Sorted by `admin` field of embedded objects\n        - `?sortBy=admin&desc=true` -> Invert sorting\n- Individual Resource (e.g. `/users/scmadmin`)\n    - Exists  -> 200\n",
        "\n### DELETE\n\n- existing -> 204\n- not existing -> 204\n- without permission -> 401\n\n## Exemplary calls & Resource specific test <|[[--cases--]]|>\n\nIn order to extend those tests to other Resources, have a look at the rest docs. Note that the Content Type is specific to each resource as well.\n"
      ]
    }
  },
  _links: {}
};

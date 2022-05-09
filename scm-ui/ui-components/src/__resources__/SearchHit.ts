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
        '  public <|[[--JwtAccessTokenBuilder--]]|> custom(String key, Object value) {\n    Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "null or empty value not allowed");\n    Preconditions.checkArgument(value != null, "null or empty value not allowed");\n',
      ],
      matchesContentStart: false,
      matchesContentEnd: false,
    },
  },
  _links: {},
};

export const bashHit: Hit = {
  score: 2.5,
  fields: {
    content: {
      highlighted: true,
      fragments: [
        '# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n# SOFTWARE.\n#\n\n<|[[--getent--]]|> group scm >/dev/null || groupadd -r scm\n<|[[--getent--]]|> passwd scm >/dev/null || \\\n    useradd -r -g scm -M \\\n    -s /sbin/nologin -d /var/lib/scm \\\n    -c "user for the scm-server process" scm\nexit 0\n\n',
      ],
      matchesContentStart: false,
      matchesContentEnd: true,
    },
  },
  _links: {},
};

export const markdownHit: Hit = {
  score: 2.5,
  fields: {
    content: {
      highlighted: true,
      fragments: [
        "---\ntitle: SCM-Manager v2 Test <|[[--Cases--]]|>\n---\n\nDescribes the expected behaviour for SCMM v2 REST Resources using manual tests.\n\nThe following states general test <|[[--cases--]]|> per HTTP Method and en expected return code as well as exemplary curl calls.\nResource-specifics are stated \n\n## Test <|[[--Cases--]]|>\n\n### GET\n\n- Collection Resource (e.g. `/users`)\n    - Without parameters -> 200\n    - Parameters\n        - `?pageSize=1` -> Only one embedded element, pageTotal reflects the correct number of pages, `last` link points to last page.\n        - `?pageSize=1&page=1` -> `next` link points to page 0 ; `prev` link points to page 2\n        - `?sortBy=admin` -> Sorted by `admin` field of embedded objects\n        - `?sortBy=admin&desc=true` -> Invert sorting\n- Individual Resource (e.g. `/users/scmadmin`)\n    - Exists  -> 200\n",
        "\n### DELETE\n\n- existing -> 204\n- not existing -> 204\n- without permission -> 401\n\n## Exemplary calls & Resource specific test <|[[--cases--]]|>\n\nIn order to extend those tests to other Resources, have a look at the rest docs. Note that the Content Type is specific to each resource as well.\n",
      ],
      matchesContentStart: true,
      matchesContentEnd: false,
    },
  },
  _links: {},
};

export const filenameXmlHit: Hit = {
  score: 10.592262,
  fields: {
    content: {
      highlighted: false,
      value:
        '<?xml version="1.0"?>\n<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">\n\n  <modelVersion>4.0.0</modelVersion>\n\n  <parent>\n    <artifactId>scm-clients</artifactId>\n    <groupId>sonia.scm.clients</groupId>\n    <version>2.0.0-SNAPSHOT</version>\n  </parent>\n\n  <artifactId>scm-cli-client</artifactId>\n  <version>2.0.0-SNAPSHOT</version>\n  <name>scm-cli-client</name>\n\n  <dependencies>\n\n    <!-- fix javadoc -->\n\n    <dependency>\n      <groupId>javax.servlet</groupId>\n      <artifactId>javax.servlet-api</artifactId>\n      <version>${servlet.version}</version>\n    </dependency>\n\n    <dependency>\n      <groupId>javax.transaction</groupId>\n      <artifactId>jta</artifactId>\n      <version>1.1</version>\n      <scope>provided</scope>\n    </dependency>\n\n    <dependency>\n      <groupId>sonia.scm.clients</groupId>\n      <artifactId>scm-client-impl</artifactId>\n      <version>2.0.0-SNAPSHOT</version>\n    </dependency>\n\n    <dependency>\n      <groupId>args4j</groupId>\n      <artifactId>args4j</artifactId>\n      <version>2.0.29</version>\n    </dependency>\n\n    <dependency>\n      <groupId>ch.qos.logback</groupId>\n      <artifactId>logback-classic</artifactId>\n      <version>${logback.version}</version>\n    </dependency>\n\n    <dependency>\n      <groupId>org.freemarker</groupId>\n      <artifactId>freemarker</artifactId>\n      <version>2.3.21</version>\n    </dependency>\n\n  </dependencies>\n\n  <build>\n    <plugins>\n\n      <plugin>\n        <groupId>com.mycila.maven-license-plugin</groupId>\n        <artifactId>maven-license-plugin</artifactId>\n        <version>1.9.0</version>\n        <configuration>\n          <header>http://download.scm-manager.org/licenses/mvn-license.txt</header>\n          <includes>\n            <include>src/**</include>\n            <include>**/test/**</include>\n          </includes>\n          <excludes>\n            <exclude>target/**</exclude>\n            <exclude>.hg/**</exclude>\n            <exclude>**/*.ftl</exclude>\n          </excludes>\n          <strictCheck>true</strictCheck>\n        </configuration>\n      </plugin>\n\n      <plugin>\n        <groupId>org.apache.maven.plugins</groupId>\n        <artifactId>maven-assembly-plugin</artifactId>\n        <version>2.3</version>\n        <configuration>\n          <archive>\n            <manifest>\n              <mainClass>sonia.scm.cli.App</mainClass>\n            </manifest>\n          </archive>\n          <descriptorRefs>\n            <descriptorRef>jar-with-dependencies</descriptorRef>\n          </descriptorRefs>\n        </configuration>\n        <executions>\n          <execution>\n            <phase>package</phase>\n            <goals>\n              <goal>single</goal>\n            </goals>\n          </execution>\n        </executions>\n      </plugin>\n\n    </plugins>\n  </build>\n\n  </project>\n',
    },
  },
  _links: {},
};

export const pullRequestHit: Hit = {
  score: 0.2837065,
  fields: {
    description: {
      highlighted: false,
      value:
        "The Hitchhiker's Guide to the Galaxy (sometimes referred to as **HG2G**, **HHGTTG** or **H2G2**) is a comedy science fiction series created by Douglas Adams. Originally a radio comedy broadcast on BBC Radio 4 in 1978, it was later adapted to other formats, including stage shows, novels, comic books, a 1981 TV series, a 1984 video game, and 2005 feature film.\n\nThis fixes a SQL Injection, a Race condition and a XSS\n\nA prominent series in British popular culture, The Hitchhiker's Guide to the Galaxy has become an international multi-media phenomenon; the novels are the most widely distributed, having been translated into more than 30 languages by 2005. In 2017, BBC Radio 4 announced a 40th-anniversary celebration with Dirk Maggs, one of the original producers, in charge. This sixth series of the sci-fi spoof has been based on Eoin Colfer's book And Another Thing, with additional unpublished material by Douglas Adams. The first of six new episodes was broadcast on 8 March 2018.",
    },
  },
  _links: {},
};

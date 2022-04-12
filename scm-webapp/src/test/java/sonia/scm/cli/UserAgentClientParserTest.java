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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserAgentClientParserTest {


  @Test
  void shouldParseNameWithSpace() {
    Client client = UserAgentClientParser.parse("scm cli/1.0.0 (Linux x86_64; bb4d50d; 2022-04-04T09:41:03Z)");

    assertThat(client.getName()).isEqualTo("scm cli");
    assertThat(client.getVersion()).isEqualTo("1.0.0");
  }

  @Test
  void shouldParseEmptyUserAgent() {
    Client client = UserAgentClientParser.parse("");

    assertThat(client.getName()).isEqualTo("unknown");
    assertThat(client.getVersion()).isEqualTo("unknown");
  }

  @Test
  void shouldParseBrokenUserAgent() {
    Client client = UserAgentClientParser.parse("scm-cli/1.0.0 ()");

    assertThat(client.getName()).isEqualTo("scm-cli");
    assertThat(client.getVersion()).isEqualTo("1.0.0");
  }

  @Test
  void shouldParseUserAgentWithNameOnly() {
    Client client = UserAgentClientParser.parse("scm-cli");

    assertThat(client.getName()).isEqualTo("scm-cli");
    assertThat(client.getVersion()).isEqualTo("unknown");
  }

  @Test
  void shouldParseDevVersion() {
    Client client = UserAgentClientParser.parse("scm-cli/x.y.z (Darwin arm64)");
    assertThat(client.getName()).isEqualTo("scm-cli");
    assertThat(client.getVersion()).isEqualTo("x.y.z");
    assertThat(client.getOs()).hasValueSatisfying(v -> assertThat(v).isEqualTo("Darwin"));
    assertThat(client.getArch()).hasValueSatisfying(v -> assertThat(v).isEqualTo("arm64"));
  }

  @Test
  void shouldParseCurlClient() {
    Client client = UserAgentClientParser.parse("curl/7.6.3");
    assertThat(client.getName()).isEqualTo("curl");
    assertThat(client.getVersion()).isEqualTo("7.6.3");
  }

  @Nested
    class RealWorldUseCases {
    Client client = UserAgentClientParser.parse("scm-cli/1.0.0 (Linux x86_64; bb4d50d; 2022-04-04T09:41:03Z)");

    @Test
    void shouldParseName() {
      assertThat(client.getName()).isEqualTo("scm-cli");
    }

    @Test
    void shouldParseVersion() {
      assertThat(client.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void shouldParseOs() {
      assertThat(client.getOs()).hasValueSatisfying(v -> assertThat(v).isEqualTo("Linux"));
    }

    @Test
    void shouldParseArch() {
      assertThat(client.getArch()).hasValueSatisfying(v -> assertThat(v).isEqualTo("x86_64"));
    }

    @Test
    void shouldParseCommitHash() {
      assertThat(client.getCommitHash()).hasValueSatisfying(v -> assertThat(v).isEqualTo("bb4d50d"));
    }

    @Test
    void shouldParseBuildTime() {
      ZonedDateTime time = ZonedDateTime.of(2022, 4, 4, 9, 41, 3, 0, ZoneId.of("UTC"));
      assertThat(client.getBuildTime()).hasValueSatisfying(v -> assertThat(v).isEqualTo(time.toInstant()));
    }
  }

}

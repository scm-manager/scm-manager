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

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

package sonia.scm.xml;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XmlEncryptionAdapterTest {

  private final static String API_TOKEN = "113bb79d12c179301b93e9ff1ad32181a0";

  private final XmlEncryptionAdapter xmlEncryptionAdapter = new XmlEncryptionAdapter();

  @Test
  void shouldEncryptTokenOnMarshalling() {
    String marshalledToken = xmlEncryptionAdapter.marshal(API_TOKEN);

    assertThat(marshalledToken).isNotEqualTo(API_TOKEN);
    assertThat(marshalledToken).startsWith("{enc}");
  }

  @Test
  void shouldUnmarshallEncryptedToken() {
    String marshalledToken = xmlEncryptionAdapter.marshal(API_TOKEN);

    String unmarshalledToken = xmlEncryptionAdapter.unmarshal(marshalledToken);

    assertThat(unmarshalledToken).isEqualTo(API_TOKEN);
  }

  @Test
  void shouldUnmarshallNotEncryptedToken() {
    String unmarshalledToken = xmlEncryptionAdapter.unmarshal(API_TOKEN);

    assertThat(unmarshalledToken).isEqualTo(API_TOKEN);
  }
}

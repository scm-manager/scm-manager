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

package sonia.scm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;

import java.io.IOException;

public final class SecureParameterSerializer {

  private final ObjectMapper mapper;

  @Inject
  public SecureParameterSerializer(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public String serialize(Object object) throws IOException {
    String json = mapper.writeValueAsString(object);
    return CipherUtil.getInstance().encode(json);
  }

  public <T> T deserialize(String serialized, Class<T> type) throws IOException {
    String decoded = CipherUtil.getInstance().decode(serialized);
    return mapper.readValue(decoded, type);
  }

}

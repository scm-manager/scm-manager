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

package sonia.scm.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXB;

import java.io.StringReader;
import java.io.StringWriter;

public class SerializationTestUtil {

  public static <T> T toAndFromJson(Class<T> clazz, T input) throws JsonProcessingException {
    final ObjectMapper objectMapper = new ObjectMapper();
    final String json = objectMapper.writeValueAsString(input);
    return objectMapper.readValue(json, clazz);
  }

  public static <T> T toAndFromXml(Class<T> clazz, T input) {
    final StringWriter xmlWriter = new StringWriter();
    JAXB.marshal(input, xmlWriter);
    final StringReader xmlReader = new StringReader(xmlWriter.toString());
    return JAXB.unmarshal(xmlReader, clazz);
  }

  public static <T> T toAndFromJsonAndXml(Class<T> clazz, T input) throws JsonProcessingException {
    return toAndFromXml(clazz, toAndFromJson(clazz, input));
  }
}

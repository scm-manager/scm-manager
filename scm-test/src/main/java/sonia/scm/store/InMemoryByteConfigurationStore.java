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

import jakarta.xml.bind.JAXB;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * In memory store implementation of {@link ConfigurationStore} using a byte array to store the serialized object.
 */
public class InMemoryByteConfigurationStore<T> implements ConfigurationStore<T> {

  private Class<T> type;
  byte[] store;

  public InMemoryByteConfigurationStore(Class<T> type) {
    this.type = type;
  }

  @Override
  public T get() {
    if (store != null) {
      return JAXB.unmarshal(new ByteArrayInputStream(store), type);
    }
    return null;
  }

  @Override
  public void set(T object) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(object, baos);
    store = baos.toByteArray();
  }

  /**
   * This method can be used to mock stores with old types to test update steps or otherwise the compatability of
   * objects with old versions.
   */
  public void setOldObject(Object object) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(object, baos);
    store = baos.toByteArray();
  }

  /**
   * This method can be used to mock stores with raw xml data to test update steps or otherwise the compatability of
   * objects with old versions.
   */
  public void setRawXml(String xml) {
    store = xml.getBytes(StandardCharsets.UTF_8);
  }

  void overrideType(Class<T> type) {
    this.type = type;
  }
}

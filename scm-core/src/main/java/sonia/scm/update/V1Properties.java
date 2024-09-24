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

package sonia.scm.update;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.empty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "properties")
public class V1Properties {
  @XmlElement(name = "item")
  private List<V1Property> properties;

  public V1Properties() {
  }

  public V1Properties(V1Property... properties) {
    this(asList(properties));
  }

  public V1Properties(List<V1Property> properties) {
    this.properties = properties;
  }

  public String get(String key) {
    return getOptional(key).orElse(null);
  }

  public Optional<String> getOptional(String key) {
    return streamProps()
      .filter(prop -> prop.getValue() != null)
      .filter(p -> key.equals(p.getKey()))
      .map(V1Property::getValue)
      .findFirst();
  }

  public Optional<Boolean> getBoolean(String key) {
    return getOptional(key).map(Boolean::valueOf);
  }

  public  <T extends Enum<T>> Optional<T> getEnum(String key, Class<T> enumType) {
    return getOptional(key).map(name -> Enum.valueOf(enumType, name));
  }

  public boolean hasAny(String[] keys) {
    return streamProps().anyMatch(p -> stream(keys).anyMatch(k -> k.equals(p.getKey())));
  }

  public boolean hasAll(String[] keys) {
    return stream(keys).allMatch(k -> streamProps().anyMatch(p -> k.equals(p.getKey())));
  }

  private Stream<V1Property> streamProps() {
    return properties == null? empty(): properties.stream();
  }
}

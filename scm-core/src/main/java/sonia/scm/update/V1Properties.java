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
    
package sonia.scm.update;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
    return streamProps().filter(p -> key.equals(p.getKey())).map(V1Property::getValue).findFirst();
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

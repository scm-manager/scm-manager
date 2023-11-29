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

package sonia.scm.security;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "keys")
class ApiKeyCollection {
  @XmlElement(name = "key")
  private Collection<ApiKeyWithPassphrase> keys;

  public ApiKeyCollection add(ApiKeyWithPassphrase key) {
    Collection<ApiKeyWithPassphrase> newKeys;
    if (CollectionUtils.isEmpty(keys)) {
      newKeys = singletonList(key);
    } else {
      newKeys = new ArrayList<>(keys.size() + 1);
      newKeys.addAll(keys);
      newKeys.add(key);
    }
    return new ApiKeyCollection(newKeys);
  }

  public ApiKeyCollection remove(Predicate<ApiKeyWithPassphrase> predicate) {
    Collection<ApiKeyWithPassphrase> newKeys = keys.stream().filter(key -> !predicate.test(key)).collect(toList());
    return new ApiKeyCollection(newKeys);
  }
}

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

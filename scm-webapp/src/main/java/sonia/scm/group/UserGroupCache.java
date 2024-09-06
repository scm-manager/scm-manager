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

package sonia.scm.group;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.xml.XmlMapMultiStringAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

@AuditEntry(ignore = true)
@XmlRootElement(name = "user-group-cache")
@XmlAccessorType(XmlAccessType.FIELD)
class UserGroupCache {
  @XmlJavaTypeAdapter(XmlMapMultiStringAdapter.class)
  private Map<String, Set<String>> cache;

  Set<String> get(String user) {
    if (cache == null) {
      return emptySet();
    }
    return cache.getOrDefault(user, emptySet());
  }

  boolean put(String user, Set<String> groups) {
    if (cache == null) {
      cache = new HashMap<>();
    }
    if (groups.equals(cache.get(user))) {
      return false;
    }
    cache.put(user, groups);
    return true;
  }
}

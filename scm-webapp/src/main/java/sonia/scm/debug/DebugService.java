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

package sonia.scm.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.security.Role;

import java.util.Collection;

/**
 * The DebugService stores and returns received data from repository hook events.
 *
 */
@Singleton
public final class DebugService
{

  private final Multimap<NamespaceAndName,DebugHookData> receivedHooks = LinkedListMultimap.create();

  /**
   * Store {@link DebugHookData} for the given repository.
   */
  void put(NamespaceAndName namespaceAndName, DebugHookData hookData)
  {
    receivedHooks.put(namespaceAndName, hookData);
  }
  
  /**
   * Returns the last received hook data for the given repository.
   */
  public DebugHookData getLast(NamespaceAndName namespaceAndName){
    // debug permission does not exists, so only accounts with "*" permission can use these resource
    SecurityUtils.getSubject().checkPermission("debug");
    DebugHookData hookData = null;
    Collection<DebugHookData> receivedHookData = receivedHooks.get(namespaceAndName);
    if (receivedHookData != null && ! receivedHookData.isEmpty()){
      hookData = Iterables.getLast(receivedHookData);
    }
    return hookData;
  }
  
  /**
   * Returns all received hook data for the given repository.
   */
  public Collection<DebugHookData> getAll(NamespaceAndName namespaceAndName){
    // debug permission does not exists, so only accounts with "*" permission can use these resource
    SecurityUtils.getSubject().checkPermission("debug");
    return receivedHooks.get(namespaceAndName);
  }
}

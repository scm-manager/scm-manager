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

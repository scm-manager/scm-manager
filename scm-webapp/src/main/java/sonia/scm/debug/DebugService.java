/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Singleton;
import java.util.Collection;
import org.apache.shiro.SecurityUtils;
import sonia.scm.security.Role;

/**
 * The DebugService stores and returns received data from repository hook events.
 *
 * @author Sebastian Sdorra
 */
@Singleton
public final class DebugService
{

  private final Multimap<String,DebugHookData> receivedHooks = LinkedListMultimap.create();

  /**
   * Stores {@link DebugHookData} for the given repository.
   * 
   * @param repository repository id
   * @param hookData received hook data
   */
  void put(String repository, DebugHookData hookData)
  {
    receivedHooks.put(repository, hookData);
  }
  
  /**
   * Returns the last received hook data for the given repository.
   * 
   * @param repository repository id
   * 
   * @return the last received hook data for the given repository
   */
  public DebugHookData getLast(String repository){
    SecurityUtils.getSubject().checkRole(Role.ADMIN);
    DebugHookData hookData = null;
    Collection<DebugHookData> receivedHookData = receivedHooks.get(repository);
    if (receivedHookData != null && ! receivedHookData.isEmpty()){
      hookData = Iterables.getLast(receivedHookData);
    }
    return hookData;
  }
  
  /**
   * Returns all received hook data for the given repository.
   * 
   * @param repository repository id
   * 
   * @return all received hook data for the given repository
   */
  public Collection<DebugHookData> getAll(String repository){
    SecurityUtils.getSubject().checkRole(Role.ADMIN);
    return receivedHooks.get(repository);
  }
  
}

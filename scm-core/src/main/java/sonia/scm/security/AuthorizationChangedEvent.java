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
package sonia.scm.security;

import sonia.scm.event.Event;

/**
 * This type of event is fired whenever a authorization relevant data changes. This event
 * is especially useful for cache invalidation.
 * 
 * @author Sebastian Sdorra
 * @since 1.52
 */
@Event
public final class AuthorizationChangedEvent {
  
  private final String nameOfAffectedUser;
  
  private AuthorizationChangedEvent(String nameOfAffectedUser) {
    this.nameOfAffectedUser = nameOfAffectedUser;
  }
  
  /**
   * Returns {@code true} if every user is affected by this data change.
   * 
   * @return {@code true} if every user is affected
   */
  public boolean isEveryUserAffected(){
    return nameOfAffectedUser != null;
  }
  
  /**
   * Returns the name of the user which is affected by this event.
   * 
   * @return name of affected user
   */
  public String getNameOfAffectedUser(){
    return nameOfAffectedUser;
  }
  
  /**
   * Creates a new event which affects every user.
   * 
   * @return new event for every user
   */
  public static AuthorizationChangedEvent createForEveryUser() {
    return new AuthorizationChangedEvent(null);
  }
  
  /**
   * Create a new event which affect a single user.
   * 
   * @param nameOfAffectedUser name of affected user
   * 
   * @return new event for a single user
   */
  public static AuthorizationChangedEvent createForUser(String nameOfAffectedUser) {
    return new AuthorizationChangedEvent(nameOfAffectedUser);
  }
  
}

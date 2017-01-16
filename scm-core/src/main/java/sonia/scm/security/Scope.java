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

import com.google.common.collect.ImmutableList;
import java.util.Collection;

import java.util.Collections;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Scope of a token. A scope is able to reduce the permissions of a token authentication. That means we can issue a 
 * token which is only suitable for a single or a set of actions e.g.: we could create a token which can only read
 * a single repository. The values of the scope should be explicit string representations of shiro permissions. An empty
 * scope means all permissions of the user.
 * 
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@XmlRootElement(name = "scope")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Scope implements Iterable<String> {

  private static final Scope EMPTY = new Scope(Collections.emptySet());
  
  private final Collection<String> values;
  
  private Scope(Collection<String> values) {
    this.values = values;
  }

  @Override
  public Iterator<String> iterator() {
    return values.iterator();
  }
  
  /**
   * Returns {@code true} if the scope is empty.
   * 
   * @return {@code true} if the scope is empty
   */
  public boolean isEmpty() {
    return values.isEmpty();
  }
  
  /**
   * Creates an empty scope.
   * 
   * @return empty scope
   */
  public static Scope empty(){
    return EMPTY;
  }
  
  /**
   * Creates a scope object from the given iterable.
   * 
   * @param values values of scope
   * 
   * @return new scope
   */
  public static Scope valueOf(Iterable<String> values) {
    return new Scope(ImmutableList.copyOf(values));
  }
  
  /**
   * Create a scope from the given array.
   * 
   * @param values values of scope
   * 
   * @return new scope.
   */
  public static Scope valueOf(String... values) {
    return new Scope(ImmutableList.copyOf(values));
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("[");
    Iterator<String> it = values.iterator();
    while (it.hasNext()) {
      buffer.append('"').append(it.next()).append('"');
      if (it.hasNext()) {
        buffer.append(", ");
      }
    }
    return buffer.append("]").toString();
  }
  
}

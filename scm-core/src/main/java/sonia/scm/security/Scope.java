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

import com.google.common.collect.ImmutableList;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

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

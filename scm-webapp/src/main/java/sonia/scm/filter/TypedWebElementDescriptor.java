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
    
package sonia.scm.filter;

import java.util.Objects;

import sonia.scm.plugin.WebElementDescriptor;

/**
 *
 * @author Sebastian Sdorra
 * @param <T>
 * @since 2.0.0
 */
public final class TypedWebElementDescriptor<T> 
{
  private final Class<T> clazz;
  private final WebElementDescriptor descriptor;

  public TypedWebElementDescriptor(Class<T> clazz,
    WebElementDescriptor descriptor)
  {
    this.clazz = clazz;
    this.descriptor = descriptor;
  }

  public Class<T> getClazz()
  {
    return clazz;
  }

  public WebElementDescriptor getDescriptor()
  {
    return descriptor;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(clazz, descriptor);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    
    final TypedWebElementDescriptor other = (TypedWebElementDescriptor) obj;
    return Objects.equals(clazz, other.clazz)
      && Objects.equals(descriptor, other.descriptor);
  }
  
}

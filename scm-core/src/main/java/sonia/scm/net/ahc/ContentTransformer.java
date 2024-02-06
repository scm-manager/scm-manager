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
    
package sonia.scm.net.ahc;

import com.google.common.io.ByteSource;
import sonia.scm.plugin.ExtensionPoint;

/**
 * Transforms {@link ByteSource} content to an object and vice versa. This class
 * is an extension point, this means that plugins can define their own
 * {@link ContentTransformer} implementations by implementing the interface and
 * annotate the implementation with the {@link sonia.scm.plugin.Extension}
 * annotation.
 *
 * @since 1.46
 */
@ExtensionPoint
public interface ContentTransformer
{
  
  /**
   * Returns {@code true} if the transformer is responsible for the given 
   * object and content type.
   * 
   * @param type object type
   * @param contentType content type
   */
  public boolean isResponsible(Class<?> type, String contentType);

  /**
   * Marshalls the given object into a {@link ByteSource}.
   *
   *
   * @param object object to marshall
   *
   * @return string content
   */
  public ByteSource marshall(Object object);

  /**
   * Unmarshall the {@link ByteSource} content to an object of the given type.
   *
   *
   * @param type type of result object
   * @param content content
   * @param <T> type of result object
   */
  public <T> T unmarshall(Class<T> type, ByteSource content);
}

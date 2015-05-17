/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.net.ahc;

import com.google.common.io.ByteSource;
import sonia.scm.plugin.ExtensionPoint;

/**
 * Transforms {@link ByteSource} content to an object and vice versa. This class
 * is an extension point, this means that plugins can define their own
 * {@link ContentTransformer} implementations by implementing the interface and
 * annotate the implementation with the {@link sonia.scm.plugin.ext.Extension} 
 * annotation.
 *
 * @author Sebastian Sdorra
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
   * 
   * @return {@code true} if the transformer is responsible
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
   *
   * @return
   */
  public <T> T unmarshall(Class<T> type, ByteSource content);
}

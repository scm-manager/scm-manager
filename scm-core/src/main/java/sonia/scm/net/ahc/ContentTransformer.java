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

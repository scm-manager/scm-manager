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

package sonia.scm.search;

import com.google.common.annotations.Beta;

/**
 * Can be used to index objects for full text searches.
 *
 * @since 2.21.0
 */
@Beta
public interface Index<T> extends AutoCloseable {

  /**
   * Store the given object in the index.
   * All fields of the object annotated with {@link Indexed} will be stored in the index.
   *
   * @param id identifier of the object in the index
   * @param permission Shiro permission string representing the required permission to see this object as a result
   * @param object object to index
   *
   * @see Indexed
   */
  void store(Id id, String permission, T object);

  void deleteAll();

  /**
   * Delete the object with the given id and type from index.
   *
   * @param id id of object
   */
  void delete(Id id);

  /**
   * Delete all objects which are related the given repository from index.
   *
   * @param repositoryId id of repository
   */
  void deleteByRepository(String repositoryId);

  /**
   * Delete all objects with the given type from index.
   * This method is mostly if the index type has changed and the old type (in form of class) is no longer available.
   * @param typeName type name of objects
   */
  void deleteByTypeName(String typeName);

  /**
   * Close index and commit changes.
   */
  @Override
  void close();
}

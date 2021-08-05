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
 * @param <T> type of indexed objects
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

  /**
   * Delete provides an api to delete objects from the index
   * @return delete api
   * @since 2.23.0
   */
  Deleter delete();

  /**
   * Close index and commit changes.
   */
  @Override
  void close();

  /**
   * Deleter provides an api to delete object from index.
   *
   * @since 2.23.0
   */
  interface Deleter {

    /**
     * Returns an api which allows deletion of objects from the type of this index.
     * @return type restricted delete api
     */
    ByTypeDeleter byType();

    /**
     * Returns an api which allows deletion of objects of every type.
     * @return unrestricted delete api for all types.
     */
    AllTypesDeleter allTypes();
  }

  /**
   * Delete api for the type of the index. This means, that only entries for this
   * type will be deleted.
   *
   * @since 2.23.0
   */
  interface ByTypeDeleter {

    /**
     * Delete the object with the given id and type from index.
     * @param id id of object
     */
    void byId(Id id);

    /**
     * Delete all objects of the given type from index.
     */
    void all();

    /**
     * Delete all objects which are related the given type and repository from index.
     *
     * @param repositoryId id of repository
     */
    void byRepository(String repositoryId);
  }

  /**
   * Delete api for the overall index regarding all types.
   *
   * @since 2.23.0
   */
  interface AllTypesDeleter {

    /**
     * Delete all objects which are related to the given repository from index regardless of their type.
     * @param repositoryId repository id
     */
    void byRepository(String repositoryId);

    /**
     * Delete all objects with the given type from index.
     * This method is mostly useful if the index type has changed and the old type (in form of a class)
     * is no longer available.
     * @param typeName type name of objects
     */
    void byTypeName(String typeName);
  }
}

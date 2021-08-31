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
import sonia.scm.repository.Repository;

/**
 * Can be used to index objects for full text searches.
 * @param <T> type of indexed objects
 * @since 2.21.0
 */
@Beta
public interface Index<T>  {

  /**
   * Returns details such as name and type of index.
   *
   * @return details of index
   * @since 2.23.0
   */
  IndexDetails getDetails();

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
  void store(Id<T> id, String permission, T object);

  /**
   * Delete provides an api to delete objects from the index
   * @return delete api
   * @since 2.23.0
   */
  Deleter<T> delete();

  /**
   * Deleter provides an api to delete object from index.
   *
   * @since 2.23.0
   */
  interface Deleter<T> {

    /**
     * Delete the object with the given id and type from index.
     * @param id id of object
     */
    void byId(Id<T> id);

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

    /**
     * Delete all objects which are related the given type and repository from index.
     *
     * @param repository repository
     */
    default void byRepository(Repository repository) {
      byRepository(repository.getId());
    }
  }

}

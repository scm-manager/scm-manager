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

package sonia.scm.search;

import com.google.common.annotations.Beta;
import sonia.scm.ModelObject;
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
     * Returns a builder api to delete objects with the given part of the id.
     * @param type type of id part
     * @param id value of id part
     * @return builder delete api
     * @see Id#and(Class, String)
     */
    DeleteBy by(Class<?> type, String id);

    /**
     * Returns a builder api to delete objects with the given part of the id.
     * @param type type of id part
     * @param idObject object which holds the id value
     * @return builder delete api
     * @see Id#and(Class, ModelObject)
     */
    default DeleteBy by(Class<?> type, ModelObject idObject) {
      return by(type, idObject.getId());
    }

    /**
     * Returns a builder api to delete objects with the given repository part of the id.
     * @param repository repository part of the id
     * @return builder delete api
     * @see Id#and(Class, ModelObject)
     */
    default DeleteBy by(Repository repository) {
      return by(Repository.class, repository);
    }
  }

  /**
   * Builder api to delete all objects which match parts of their id.
   * @since 2.23.0
   * @see Id#and(Class, String)
   */
  interface DeleteBy {

    /**
     * Select only objects for deletion which are matching the previous id parts and the given one.
     * @param type type of id part
     * @param id value of id part
     * @return {@code this}
     */
    DeleteBy and(Class<?> type, String id);

    /**
     * Select only objects for deletion which are matching the previous id parts and the given one.
     * @param type type of id part
     * @param idObject object which holds the id value
     * @return {@code this}
     */
    default DeleteBy and(Class<?> type, ModelObject idObject) {
      return and(type, idObject.getId());
    }

    /**
     * Select only objects for deletion which are matching the previous id parts and the given repository part.
     * @param repository repository part of the id
     * @return {@code this}
     */
    default DeleteBy and(Repository repository) {
      return and(Repository.class, repository.getId());
    }

    /**
     * Delete all matching objects from index.
     */
    void execute();
  }

}

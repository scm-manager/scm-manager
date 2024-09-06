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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import sonia.scm.ModelObject;
import sonia.scm.repository.Repository;

import java.util.Collections;
import java.util.Map;

/**
 * Describes the id of an indexed object.
 *
 * @since 2.21.0
 */
@Beta
@ToString
@EqualsAndHashCode
@Getter(AccessLevel.PACKAGE)
public final class Id<T> {

  private final Class<T> mainType;
  private final String mainId;
  private final Map<Class<?>, String> others;

  private Id(Class<T> mainType, String mainId, Map<Class<?>, String> others) {
    this.mainType = mainType;
    this.mainId = mainId;
    this.others = others;
  }

  /**
   * Creates a new combined id by adding a new type and value.
   * @param type other type
   * @param id other id
   * @since 2.23.0
   */
  public Id<T> and(Class<?> type, String id) {
    Preconditions.checkArgument(type != null, "type is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "id is required");
    return new Id<>(
      mainType,
      mainId,
      ImmutableMap.<Class<?>, String>builder()
        .putAll(others)
        .put(type, id)
        .build()
    );
  }

  /**
   * Creates a new combined id by adding a new type and value.
   * @param type other type
   * @param idObject object which holds id
   * @since 2.23.0
   */
  public Id<T> and(Class<?> type, ModelObject idObject) {
    Preconditions.checkArgument(idObject != null, "id object is required");
    return and(type, idObject.getId());
  }

  /**
   * Creates a new combined id by adding the given repository.
   * @param repository repository to add
   * @since 2.23.0
   */
  public Id<T> and(Repository repository) {
    Preconditions.checkArgument(repository != null, "repository is required");
    return and(Repository.class, repository);
  }

  /**
   * Creates a new id.
   *
   * @param mainType main type of the id
   * @param mainId main id of the id
   */
  public static <T> Id<T> of(Class<T> mainType, String mainId) {
    Preconditions.checkArgument(mainType != null, "main type is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(mainId), "main id is required");
    return new Id<>(mainType, mainId, Collections.emptyMap());
  }

  /**
   * Creates a new id.
   *
   * @param mainType main type of the id
   * @param mainIdObject object which holds the main id
   */
  public static <T> Id<T> of(Class<T> mainType, ModelObject mainIdObject) {
    Preconditions.checkArgument(mainIdObject != null, "main id object is required");
    return of(mainType, mainIdObject.getId());
  }
}

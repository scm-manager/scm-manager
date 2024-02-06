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

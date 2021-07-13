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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sonia.scm.ModelObject;
import sonia.scm.repository.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Describes the id of an indexed object.
 *
 * @since 2.21.0
 */
@ToString
@EqualsAndHashCode
public final class Id {

  private final String value;
  private final String repository;

  private Id(@Nonnull String value, @Nullable String repository) {
    this.value = value;
    this.repository = repository;
  }

  /**
   * Returns the string representation of the id without the repository part.
   *
   * @return string representation without repository.
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the repository id part of the id or an empty optional if the id does not belong to a repository.
   * @return repository id or empty
   */
  public Optional<String> getRepository() {
    return Optional.ofNullable(repository);
  }

  /**
   * Creates the id with the id of the given repository.
   * @param repository repository
   * @return id with repository id
   */
  public Id withRepository(@Nonnull Repository repository) {
    checkRepository(repository);
    return withRepository(repository.getId());
  }

  /**
   * Creates the id with the  given repository id.
   * @param repository repository id
   * @return id with repository id
   */
  public Id withRepository(@Nonnull String repository) {
    checkRepository(repository);
    return new Id(value, repository);
  }

  /**
   * Returns the string representation of the id including the repository.
   * @return string representation
   */
  public String asString() {
    if (repository != null) {
      return value + "/" + repository;
    }
    return value;
  }

  /**
   * Creates a new id.
   *
   * @param value primary value of the id
   * @param others additional values which should be part of the id
   *
   * @return new id
   */
  public static Id of(@Nonnull String value, String... others) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(value), "primary value is required");
    String idValue = value;
    if (others.length > 0) {
      idValue += ":" + Joiner.on(':').join(others);
    }
    return new Id(idValue, null);
  }

  /**
   * Creates a new id for the given repository.
   *
   * @param repository repository
   * @return id of repository
   */
  public static Id of(@Nonnull Repository repository) {
    checkRepository(repository);
    String id = repository.getId();
    checkRepository(id);
    return new Id(id, id);
  }

  /**
   * Creates a new id for the given model object.
   * @param object model object
   * @param others additional values which should be part of the id
   * @return new id from model object
   */
  public static Id of(@Nonnull ModelObject object, String... others) {
    checkObject(object);
    return of(object.getId(), others);
  }

  private static void checkRepository(Repository repository) {
    Preconditions.checkArgument(repository != null, "repository is required");
  }

  private static void checkRepository(String repository) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(repository), "repository id is required");
  }

  private static void checkObject(@Nonnull ModelObject object) {
    Preconditions.checkArgument(object != null, "object is required");
  }
}

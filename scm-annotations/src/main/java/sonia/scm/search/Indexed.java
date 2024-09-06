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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field which should be indexed.
 * @since 2.21.0
 */
@Beta
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Indexed {

  /**
   * Name of the field.
   * If not set the name of the annotated field is used.
   *
   * @return name of field
   */
  String name() default "";

  /**
   * Describes how the field is indexed.
   *
   * @return type of indexing
   */
  Type type() default Type.TOKENIZED;

  /**
   * {@code true} if this field should be part of default query for this type of object.
   */
  boolean defaultQuery() default false;

  /**
   * Boost the object if the searched query matches this field.
   * Greater than one brings the object further up in the search results.
   * Smaller than one devalues the object in the search results.
   *
   * @return boost score
   */
  float boost() default 1f;

  /**
   * {@code true} to search the field value for matches and returns fragments with those matches instead of the whole value.
   *
   */
  boolean highlighted() default false;

  /**
   * Describes how the field is analyzed and tokenized.
   *
   * @return type of analyzer
   * @since 2.23.0
   */
  Analyzer analyzer() default Analyzer.DEFAULT;

  /**
   * Describes how fields are analyzed and tokenized.
   *
   * @since 2.23.0
   */
  enum Analyzer {

    /**
     * Uses the analyzer which was used to open the index.
     */
    DEFAULT,

    /**
     * Uses an analyzer which is specialized for identifiers like repository names.
     */
    IDENTIFIER,

    /**
     * Uses an analyzer which is specialized for paths.
     */
    PATH,

    /**
     * Uses an analyzer which is specialized for source code.
     */
    CODE
  }

  /**
   * Describes how the field is indexed.
   */
  enum Type {
    /**
     * The value of the field is analyzed and split into tokens, which allows searches for parts of the value.
     * Tokenization only works for string values. If a field with another type is marked as tokenized,
     * the field is indexed as if it was marked as {@link #SEARCHABLE}.
     */
    TOKENIZED(true, true, true),

    /**
     * The value can only be searched as a whole.
     * Numeric fields can also be search as part of a range,
     * but strings are only found if the query contains the whole field value.
     */
    SEARCHABLE(false, true, true),

    /**
     * Value of the field cannot be searched for, but is returned in the result.
     */
    STORED_ONLY(false, false, true);

    private final boolean tokenized;
    private final boolean searchable;
    private final boolean stored;

    Type(boolean tokenized, boolean searchable, boolean stored) {
      this.tokenized = tokenized;
      this.searchable = searchable;
      this.stored = stored;
    }

    /**
     * @see #TOKENIZED
     */
    public boolean isTokenized() {
      return tokenized;
    }

    /**
     * @see #SEARCHABLE
     */
    public boolean isSearchable() {
      return searchable;
    }

    public boolean isStored() {
      return stored;
    }
  }
}

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
import lombok.EqualsAndHashCode;

import java.util.Locale;

/**
 * Options to configure how things are indexed and searched.
 *
 * @since 2.21.0
 */
@Beta
@EqualsAndHashCode
public class IndexOptions {

  private final Type type;
  private final Locale locale;

  private IndexOptions(Type type, Locale locale) {
    this.type = type;
    this.locale = locale;
  }

  /**
   * Returns the type of the index.
   * @return type of index
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the locale of the index content.
   *
   * @return locale of index content
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Returns the default index options which should match most of the use cases.
   *
   * @return default index options
   */
  public static IndexOptions defaults() {
    return new IndexOptions(Type.GENERIC, Locale.ENGLISH);
  }

  /**
   * Returns index options for a specific language.
   * This options should be used if the content is written in a specific natural language.
   *
   * @param locale natural language of content
   *
   * @return options for content in natural language
   */
  public static IndexOptions naturalLanguage(Locale locale) {
    return new IndexOptions(Type.NATURAL_LANGUAGE, locale);
  }

  /**
   * Type of indexing.
   */
  public enum Type {

    /**
     * Not specified content.
     */
    GENERIC,

    /**
     * Content in natural language.
     */
    NATURAL_LANGUAGE;
  }

}

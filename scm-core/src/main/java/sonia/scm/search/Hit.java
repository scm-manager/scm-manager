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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.Map;

/**
 * Represents an object which matched the search query.
 *
 * @since 2.21.0
 */
@Value
public class Hit {

  /**
   * Id of the matched object.
   */
  String id;

  /**
   * The score describes how good the match was.
   */
  float score;

  /**
   * Fields of the matched object.
   * Key of the map is the name of the field and the value is either a {@link ValueField} or a {@link HighlightedField}.
   */
  Map<String, Field> fields;

  /**
   * Base class of hit field types.
   */
  @Getter
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public abstract static class Field {
    boolean highlighted;
  }

  /**
   * A fields which holds a complete value.
   */
  @Getter
  public static class ValueField extends Field {
    Object value;

    public ValueField(Object value) {
      super(false);
      this.value = value;
    }
  }

  /**
   * A field which consist of fragments which containing a match of the searched query.
   */
  @Getter
  public static class HighlightedField extends Field {
    String[] fragments;

    public HighlightedField(String[] fragments) {
      super(true);
      this.fragments = fragments;
    }
  }

}

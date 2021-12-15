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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.Map;
import java.util.Optional;

/**
 * Represents an object which matched the search query.
 *
 * @since 2.21.0
 */
@Beta
@Value
public class Hit {

  /**
   * Id of the matched object.
   */
  String id;

  /**
   * Repository associated with the hit.
   * @since 2.23.0
   */
  String repositoryId;

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
   * Returns optional id of a repository which associated with the hit
   * or empty if the hit is not associated with any repository.
   *
   * @return optional repository id
   * @since 2.23.0
   */
  public Optional<String> getRepositoryId() {
    return Optional.ofNullable(repositoryId);
  }

  /**
   * Base class of hit field types.
   */
  @Getter
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public abstract static class Field {
    private final boolean highlighted;
  }

  /**
   * A field holding a complete value.
   */
  @Getter
  public static class ValueField extends Field {
    private final Object value;

    public ValueField(Object value) {
      super(false);
      this.value = value;
    }
  }

  /**
   * A field which consists of fragments that contain a match of the search query.
   */
  @Getter
  public static class HighlightedField extends Field {
    private final String[] fragments;

    /**
     * @since 2.28.0
     */
    private final boolean matchesContentStart;

    /**
     * @since 2.28.0
     */
    private final boolean matchesContentEnd;

    public HighlightedField(String[] fragments) {
      this(fragments, false, false);
    }

    /**
     * @since 2.28.0
     */
    public HighlightedField(String[] fragments, boolean matchesContentStart, boolean matchesContentEnd) {
      super(true);
      this.fragments = fragments;
      this.matchesContentStart = matchesContentStart;
      this.matchesContentEnd = matchesContentEnd;
    }
  }

}

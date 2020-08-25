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

package sonia.scm.repository;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a tag in a repository.
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
@EqualsAndHashCode
@ToString
@Getter
public final class Tag {

  private final String name;
  private final String revision;
  private final Long date;

  /**
   * Constructs a new tag.
   *
   * @param name     name of the tag
   * @param revision tagged revision
   */
  public Tag(String name, String revision) {
    this(name, revision, null);
  }

  /**
   * Constructs a new tag.
   *
   * @param name     name of the tag
   * @param revision tagged revision
   * @param date     the creation timestamp (milliseconds) of the tag
   *
   * @since 2.5.0
   */
  public Tag(String name, String revision, Long date) {
    this.name = name;
    this.revision = revision;
    this.date = date;
  }
}

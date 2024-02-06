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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a tag in a repository.
 *
 * @since 1.18
 */
@EqualsAndHashCode
@ToString
@Getter
public final class Tag {

  public static final String VALID_REV = "[0-9a-z]+";
  public static final Pattern VALID_REV_PATTERN = Pattern.compile(VALID_REV);

  private final String name;
  private final String revision;
  private final Long date;
  private final List<Signature> signatures = new ArrayList<>();
  private final Boolean deletable;

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
    this(name, revision, date, true);
  }

  /**
   * Constructs a new tag.
   *
   * @param name     name of the tag
   * @param revision tagged revision
   * @param date     the creation timestamp (milliseconds) of the tag
   * @param deletable whether this tag can be deleted
   *
   * @since 2.11.0
   */
  public Tag(String name, String revision, Long date, Boolean deletable) {
    this.name = name;
    this.revision = revision;
    this.date = date;
    this.deletable = deletable;
  }

  /**
   * Depending on the underlying source code management system
   * (like git or hg) and depending on the type of this tag
   * (for example git has <i>lightweight</i> and <i>annotated</i>
   * tags), this date has different meaning. For annotated tags
   * in git, this is the date the tag was created. In other cases
   * (for lightweight tags in git or all tags in hg) this is the
   * date of the referenced changeset.
   * <p>
   * Please note, that the date is retrieved in a best-effort fashion.
   * In certain situations (for example if this tag is announced in
   * a pre or post receive hook), it might not be available.
   * In these cases, this method returns an empty {@link Optional}.
   *
   * @since 2.5.0
   */
  public Optional<Long> getDate() {
    return Optional.ofNullable(date);
  }

  public void addSignature(Signature signature) {
    this.signatures.add(signature);
  }
}
